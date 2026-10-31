package kg.timmitof.keyboard.clipboard.data.di

import org.koin.core.module.Module

/** Системный буфер обмена своей платформы. */
expect val platformClipboardModule: Module
