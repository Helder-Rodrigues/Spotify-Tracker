# Spotify-Tracker
Welcome to Spotify Tracker! This project aims to create an application that serves as a database for the songs you listen to on Spotify, allowing you to rate them.

# Contents
--------
- [Description](#description)
- [Features](#features)
- [Download and Test the App](#download-and-test-the-app)
- [Environment Variables](#environment-variables)
- [Setup Environment Variables](#setup-environment-variables)
- [Running the Application](#running-the-application)
- [Usage](#usage)
- [Future Plans](#future-plans)
- [License](#license)

## Description
Spotify Tracker is a dynamic application developed in Kotlin, seamlessly connecting to your Spotify account. This innovative tool empowers you to rate the songs you listen to, providing an overview of your musical preferences. Gain insights into your favorite songs, albums and artists, and explore detailed statistics, including the last time you listened to a particular song, its duration, and how often you engage with it.

## Features
- **Intuitive Design:** Easily navigate through the information you want to know.
- **Ratings:** Rate your favorite songs!
- **Statistics:** View the songs you listen to the most and how frequently you listen to them, including the average rating for a specific album calculated from the ratings you assigned to the individual songs within that album.

## Download and Test the App
You can download this version of the app to test on your Android device. Follow the steps below:
1. Visit the [Releases](https://github.com/Helder-Rodrigues/Spotify-Tracker/releases) page.
3. Select the APK from the `debug` directory.
4. Download the APK to your Android device.
5. Install the APK on your device.
6. Open the app and log in with your Spotify account.

Please note that this version is provided for testing purposes, and you may want to build the app locally for the latest changes and features. To build the app on your own, follow the information below.

## Environment Variables
- **SPOTIFY_CLIENT_ID:**
  - Description: The client ID for Spotify integration. This variable should contain the unique identifier assigned to your Spotify application.
- **SPOTIFY_REDIRECT_URI:**
  - Description: The redirect URI for Spotify callback. Set this variable to the URI where Spotify will redirect the user after authentication.
- **DATABASE_TRACKS_REFERENCE_TEMPLATE:**
  - Description: The template for the database tracks reference. Customize this variable to define the structure of the reference used for storing tracks in the database.

## Setup Environment Variables
To run SpotApp, you need to set up the required environment variables. Follow the steps below:
1. **Create an `env` file:**
   - Navigate to the `assets` directory inside `spotApp/app/src/main/assets`.
   - Create a file named `env`.
2. **Add Environment Variables:**
   - Open the `env` file you just created.
   - Add the following environment variables:
   ```plaintext
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_REDIRECT_URI=your_spotify_redirect_uri
   DATABASE_TRACKS_REFERENCE_TEMPLATE=your_database_tracks_reference_template
   ```
Replace `your_spotify_client_id`, `your_spotify_redirect_uri`, and `your_database_tracks_reference_template` with your actual values.

Save the env file.

## Running the Application
To use the application, simply install it on your mobile device or emulator and log in with your Spotify account.
That's it! You're ready to explore and rate your favorite songs.

## Usage
Navigate through the various tabs by clicking the buttons, and use the keyboard to input ratings for different songs. Utilize the "Save" button to store the given ratings.

## Future Plans
Spotify Tracker's future plans include:
- **Detailed Artist Insights:** Know more about your favorite artists as we display an average rating based on user evaluations of their songs.
- **Intuitive Navigation:** Making navigation through the application even more user-friendly.
- **Specific Song Details:** Clicking on a specific song will unveil a cleaner interface, focusing solely on the album details and artist information of that song.
- **Improved Rating System:** Streamlining the process of giving ratings to be more intuitive and user-friendly. Giving the rating with a click, using a star-based rating system.
- **Personalized Recommendations:** Deliver personalized suggestions for tracks, albums, and artists.

## License
This project is licensed under the [MIT License](LICENSE).
