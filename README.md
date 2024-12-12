# Skin Scanner App

The **Skin Scanner App** is a mobile application designed to help users scan their skin for potential melanoma using advanced AI and computer vision techniques. This application leverages the Android Jetpack Compose framework, CameraX for custom camera overlays, and integration with Google Maps for locating nearby hospitals.

---

## Table of Contents

1. [Features](#features)
2. [Installation](#installation)
3. [Permissions](#permissions)
4. [Technical Details](#technical-details)
    - [Tools and Technologies](#tools-and-technologies)
    - [Key Components](#key-components)
5. [How It Works Together](#how-it-works-together)
6. [How to Use](#how-to-use)
7. [Contribution Guidelines](#contribution-guidelines)
8. [Acknowledgments](#acknowledgments)
9. [Authors](#authors)

---

## Features

1. **Skin Scanning with AI**:
    - Captures high-resolution images of skin.
    - Analyzes the image using AI algorithms to detect potential melanoma.

2. **Custom Camera Overlay**:
    - Provides a guided camera interface with an overlay to ensure accurate image capture.
    - Includes cropping functionality to focus on the area of interest.

3. **Google Maps Integration**:
    - Locates nearby hospitals for further consultation.

4. **Permission Handling**:
    - Requests necessary permissions for camera and location access.
    - Fallbacks for denied permissions to maintain a seamless user experience.

5. **Modern Android Architecture**:
    - Built with Jetpack Compose.
    - State management with `MutableState` for dynamic UI updates.

6. **Backend Integration**:
    - Processes captured images by sending them to the backend server for AI analysis.

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/siddarthsingotam/Skin_Scanner_App.git
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle and build the project.

4. Run the application on a physical device or emulator.

---

## Permissions

The app requires the following permissions:

- **Camera Permission**:
    - Required to capture images for skin analysis.
- **Location Permission**:
    - Required to locate nearby hospitals using Google Maps.

Make sure to grant these permissions during runtime for full functionality.

---

## Technical Details

### Tools and Technologies:
- **Kotlin**: Programming language for Android development.
- **Jetpack Compose**: Modern declarative UI toolkit.
- **CameraX**: Android Camera library for creating custom camera experiences.
- **Google Maps**: Integrated for finding nearby hospitals.
- **Retrofit**: Used for server communication.
- **Image Processing**: Includes cropping and rotating images.

### Key Components:
1. **Custom Camera Overlay**:
    - Located in `CameraPreviewWithOverlay.kt`.
    - Provides a real-time camera preview with a guided overlay.

2. **Permission Manager**:
    - Centralized logic for handling runtime permissions.
    - Ensures smooth user experience when requesting camera or location access.

3. **Skin Analysis**:
    - Captures and processes the image.
    - Sends the processed image to a backend server for AI analysis.

4. **Backend Integration**:
    - The app integrates with a backend system that processes the images and applies AI for melanoma detection.
    - Backend repository: [Skin Scanner App Backend](https://github.com/SoaresPT/Skin_Scanner_App-backend/tree/main).

5. **Google Maps Integration**:
    - Finds hospitals near the user's location using Google Maps.

---

## How It Works Together

1. The app captures high-resolution images of the skin using the custom camera overlay.
2. The captured image is sent to the backend server using Retrofit.
3. The backend processes the image using AI models to detect melanoma.
4. The result is sent back to the app, where it is displayed to the user.
5. Users can locate nearby hospitals for further consultation using the integrated Google Maps feature.

For more details about the backend, visit the [Skin Scanner App Backend Repository](https://github.com/SoaresPT/Skin_Scanner_App-backend/tree/main).

---

## How to Use

1. Launch the app.
2. Grant the necessary permissions (Camera and Location).
3. Use the **Scan** button to open the camera overlay and capture an image.
4. Analyze the captured image using the **Analyze** button.
5. Locate nearby health centers via the **Hospitals** section.
6. Clear captured images or results using the **Clear** button.

---

## Contribution Guidelines

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add feature-name"
   ```
4. Push to your branch:
   ```bash
   git push origin feature-name
   ```
5. Create a Pull Request.


---

## Acknowledgments

- Thanks to the contributors of open-source libraries like Jetpack Compose, CameraX, and Retrofit.
- Special thanks to Siddarth Singotam for maintaining this repository.
- Special thanks to SoaresPT for developing and maintaining the backend server.

---

## Authors

- [Andrii Deshko](https://github.com/LVNDLORD)
- [Siddarth Singotam](https://github.com/siddarthsingotam)
- [Sergio Soares](https://github.com/SoaresPT)

