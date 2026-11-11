"""
Тройки слов для Т9: что обычно идёт после двух предыдущих слов («как у тебя» → «дела»).

Выход — `keyboard/suggestion/data/src/main/assets/dictionaries/<code>.trigrams`, формат как у `.bigrams`:
`слово1 слово2<TAB>продолжение продолжение ...`, продолжения по убыванию частоты. Файл коммитится:
сборка не должна ходить в сеть, а корпуса — гигабайты.

Источники смешиваются поровну: частоты каждого делятся на число его слов. Так разговорные субтитры
не тонут в веб-текстах, хоть их и меньше. Поддерживаются:
- `.parquet` с колонкой `text` (FineWeb / FineWeb-2, ODC-By 1.0, https://huggingface.co/datasets/HuggingFaceFW);
- `.txt.gz` / `.txt` — предложение или документ на строку (OpenSubtitles из OPUS, https://opus.nlpl.eu).

Словарь — первые `--vocab-size` слов частотного списка языка (`dictionaries/<lang>_full.txt`): тройки
считаются только из известных слов, всё прочее (числа, латиница в русском, редкие слова) разрывает цепочку.
Конец предложения тоже разрывает: после точки клавиатура предыдущих слов не видит.

Считается в два прохода, чтобы память не росла с корпусом: сначала пары слов (контексты), из них берутся
самые частые; потом тройки — только с этими контекстами.

Запуск из корня репозитория (нужны numpy и pyarrow — лучше в отдельном venv вне проекта):

    python -I tools/trigrams.py --code ru_ru --frequency keyboard/suggestion/data/dictionaries/ru_full.txt ^
        --alphabet абвгдежзийклмнопрстуфхцчшщъыьэюя --single-letters авикосуя ^
        --source C:/corpora/fineweb/data/rus_Cyrl/train/000_00000.parquet ^
        --source C:/corpora/opensubtitles/ru.txt.gz

Лимит `--max-words` (по умолчанию 300 млн на источник) держит время в пределах десятков минут;
недокачанный `.gz` читается до места обрыва.
"""

import argparse
import gzip
import os
import re
import sys
import time
import zlib

import numpy as np

DEFAULT_OUTPUT_DIR = os.path.join("keyboard", "suggestion", "data", "src", "main", "assets", "dictionaries")

# Слово (буквы и цифры, апостроф внутри: don't) или конец предложения. Остальная пунктуация — пробел:
# запятая не мешает клавиатуре видеть предыдущее слово.
TOKEN = re.compile(r"[^\W_]+(?:['’][^\W_]+)*|[.!?…\n]")
SENTENCE_END = frozenset(".!?…\n")

BREAK = -1


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--code", required=True, help="код языка каталога, например ru_ru")
    parser.add_argument("--frequency", required=True, help="частотный список языка: `слово число` построчно")
    parser.add_argument("--alphabet", required=True, help="буквы языка; слова с другими символами не берутся")
    parser.add_argument("--source", action="append", required=True, help="корпус (.parquet / .txt.gz / .txt)")
    parser.add_argument("--single-letters", default="",
                        help="однобуквенные слова языка (singleLetters каталога); прочие буквы — обрывки вроде don't → don, t")
    parser.add_argument("--vocab-size", type=int, default=60_000)
    parser.add_argument("--max-words", type=int, default=300_000_000, help="слов из одного источника")
    parser.add_argument("--contexts", type=int, default=40_000, help="сколько пар слов оставить")
    parser.add_argument("--followers", type=int, default=8, help="продолжений на пару")
    parser.add_argument("--min-share", type=float, default=0.02,
                        help="продолжение берётся, если оно не реже этой доли самой пары")
    parser.add_argument("--output", help="файл; по умолчанию ассеты подсказок")
    return parser.parse_args()


def load_vocabulary(path, alphabet, size, single_letters=""):
    """
    Первые `size` слов частотного списка, целиком из букв алфавита (апостроф допустим).
    Одиночная буква — слово, только если она в `single_letters`.
    """
    letters = set(alphabet) | {"'"}
    words = []
    seen = set()
    with open(path, encoding="utf-8") as file:
        for line in file:
            word = fold(line.split(" ", 1)[0].strip())
            if not word or word in seen or not set(word) <= letters:
                continue
            if len(word) == 1 and word not in single_letters:
                continue
            seen.add(word)
            words.append(word)
            if len(words) == size:
                break
    return words


def fold(word):
    return word.lower().replace("ё", "е").replace("’", "'")


def read_texts(path):
    """Тексты источника по одному; битый хвост недокачанного архива просто заканчивает чтение."""
    if path.endswith(".parquet"):
        import pyarrow.parquet as pq

        for batch in pq.ParquetFile(path).iter_batches(columns=["text"], batch_size=2_000):
            yield from batch.column(0).to_pylist()
        return

    opener = gzip.open if path.endswith(".gz") else open
    try:
        with opener(path, "rt", encoding="utf-8", errors="replace") as file:
            # Строки субтитров — реплики: склеиваем пачкой, перевод строки сам разрывает предложения.
            chunk = []
            for line in file:
                chunk.append(line)
                if len(chunk) == 10_000:
                    yield "".join(chunk)
                    chunk.clear()
            if chunk:
                yield "".join(chunk)
    except (EOFError, zlib.error, gzip.BadGzipFile) as error:
        print(f"  {os.path.basename(path)}: архив оборван ({error}), беру прочитанное", file=sys.stderr)


def token_ids(path, ids, max_words):
    """Номера слов источника пачками; неизвестные слова и концы предложений — `BREAK`."""
    words = 0
    buffer = []
    for text in read_texts(path):
        for token in TOKEN.findall(text):
            if token in SENTENCE_END:
                buffer.append(BREAK)
                continue
            buffer.append(ids.get(fold(token), BREAK))
            words += 1
        buffer.append(BREAK)

        if len(buffer) >= 2_000_000:
            yield np.array(buffer, dtype=np.int64)
            buffer.clear()
        if words >= max_words:
            break
    if buffer:
        yield np.array(buffer, dtype=np.int64)


class Counter:
    """Счётчик целочисленных кодов на numpy: копит пачки, сводит их сортировкой. Огромный — забывает единицы."""

    def __init__(self, limit=60_000_000):
        self.codes = np.empty(0, dtype=np.int64)
        self.counts = np.empty(0, dtype=np.int64)
        self.pending = []
        self.pending_size = 0
        self.limit = limit

    def add(self, codes):
        self.pending.append(codes)
        self.pending_size += len(codes)
        if self.pending_size >= 20_000_000:
            self.merge()

    def merge(self):
        if not self.pending:
            return
        fresh, fresh_counts = np.unique(np.concatenate(self.pending), return_counts=True)
        self.pending.clear()
        self.pending_size = 0

        codes = np.concatenate([self.codes, fresh])
        counts = np.concatenate([self.counts, fresh_counts])
        order = np.argsort(codes, kind="stable")
        codes, counts = codes[order], counts[order]
        starts = np.flatnonzero(np.r_[True, codes[1:] != codes[:-1]])
        self.codes, self.counts = codes[starts], np.add.reduceat(counts, starts)

        if len(self.codes) > self.limit:
            keep = self.counts > 1
            self.codes, self.counts = self.codes[keep], self.counts[keep]

    def result(self):
        self.merge()
        return self.codes, self.counts


def count_source(path, ids, vocab_size, max_words, contexts=None):
    """
    Без `contexts` — пары слов, с ними — тройки, чьи первые два слова среди `contexts` (отсортированный массив).
    Возвращает коды, счётчики и число слов источника.
    """
    counter = Counter()
    words = 0
    started = time.time()
    for chunk in token_ids(path, ids, max_words):
        words += int(np.count_nonzero(chunk != BREAK))
        first, second = chunk[:-2], chunk[1:-1]
        pair = (first >= 0) & (second >= 0)

        if contexts is None:
            counter.add(first[pair] * vocab_size + second[pair])
        else:
            third = chunk[2:]
            valid = pair & (third >= 0)
            context = first[valid] * vocab_size + second[valid]
            position = np.searchsorted(contexts, context)
            known = contexts[np.minimum(position, len(contexts) - 1)] == context
            counter.add(context[known] * vocab_size + third[valid][known])

        print(f"\r  {os.path.basename(path)}: {words / 1e6:.0f} млн слов, {time.time() - started:.0f} с",
              end="", file=sys.stderr)
    print(file=sys.stderr)
    codes, counts = counter.result()
    return codes, counts, words


def mix(results):
    """Сумма долей: каждый источник весит поровну, сколько бы слов в нём ни было."""
    codes = np.concatenate([codes for codes, _, _ in results])
    weights = np.concatenate([counts / max(words, 1) for _, counts, words in results])
    order = np.argsort(codes, kind="stable")
    codes, weights = codes[order], weights[order]
    starts = np.flatnonzero(np.r_[True, codes[1:] != codes[:-1]])
    return codes[starts], np.add.reduceat(weights, starts)


def main():
    args = parse_args()
    vocabulary = load_vocabulary(args.frequency, args.alphabet, args.vocab_size, args.single_letters)
    ids = {word: index for index, word in enumerate(vocabulary)}
    size = len(vocabulary)
    print(f"{args.code}: словарь {size} слов", file=sys.stderr)

    print("Проход 1: пары слов", file=sys.stderr)
    pairs, pair_weights = mix([count_source(path, ids, size, args.max_words) for path in args.source])
    top = np.argsort(pair_weights)[::-1][: args.contexts]
    contexts = np.sort(pairs[top])
    context_weight = dict(zip(pairs[top].tolist(), pair_weights[top].tolist()))

    print("Проход 2: тройки", file=sys.stderr)
    triples, triple_weights = mix(
        [count_source(path, ids, size, args.max_words, contexts) for path in args.source]
    )

    # Тройки отсортированы по коду, значит, сгруппированы по контексту.
    triple_context = triples // size
    starts = np.flatnonzero(np.r_[True, triple_context[1:] != triple_context[:-1]])
    ends = np.r_[starts[1:], len(triples)]

    lines = []
    followers_total = 0
    for start, end in zip(starts.tolist(), ends.tolist()):
        context = int(triple_context[start])
        threshold = context_weight[context] * args.min_share
        block = start + np.argsort(-triple_weights[start:end], kind="stable")[: args.followers]
        followers = [vocabulary[int(triples[i] % size)] for i in block.tolist()
                     if triple_weights[i] >= threshold]
        if not followers:
            continue
        first, second = divmod(context, size)
        lines.append(f"{vocabulary[first]} {vocabulary[second]}\t{' '.join(followers)}")
        followers_total += len(followers)

    lines.sort()
    output = args.output or os.path.join(DEFAULT_OUTPUT_DIR, f"{args.code}.trigrams")
    with open(output, "w", encoding="utf-8", newline="\n") as file:
        file.write("\n".join(lines) + "\n")

    print(f"{output}: {len(lines)} пар, {followers_total} продолжений, "
          f"{os.path.getsize(output) / 1024:.0f} КБ", file=sys.stderr)


if __name__ == "__main__":
    main()
