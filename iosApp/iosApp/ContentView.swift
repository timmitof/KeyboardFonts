import UIKit
import SwiftUI
import Shared

/// Экраны приложения целиком на Compose: SwiftUI только хостит их.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all) // Отступы под вырез и жесты Compose считает сам (edge-to-edge, как на Android).
    }
}
