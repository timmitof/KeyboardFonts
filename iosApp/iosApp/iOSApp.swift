import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Граф Koin поднимается один раз, до первого экрана.
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
