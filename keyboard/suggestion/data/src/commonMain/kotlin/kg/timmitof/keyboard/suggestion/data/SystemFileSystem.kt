package kg.timmitof.keyboard.suggestion.data

import okio.FileSystem

/** Файловая система устройства: в общем коде okio её не объявляет — её нет у JS. */
internal expect val systemFileSystem: FileSystem
