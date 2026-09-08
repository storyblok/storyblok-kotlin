import SwiftUI
import Shared

/// Hosts the Compose Multiplatform UI built in `:shared`.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Compose draws its own status-bar inset, so let it take the whole screen; the keyboard
        // still needs the safe area so the search field is not covered.
        ComposeView()
            .ignoresSafeArea(edges: .all)
    }
}
