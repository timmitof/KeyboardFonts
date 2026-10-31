package kg.timmitof.keyboard.clipboard.data

import platform.UIKit.UIPasteboard

/** Клавиатуре iOS буфер доступен только с «Полным доступом»; без него `string` пуст. */
class IosSystemClipboardSource : SystemClipboardSource {

    override fun read(): String? = UIPasteboard.generalPasteboard.string?.toClipText()
}
