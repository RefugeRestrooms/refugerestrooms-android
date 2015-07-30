# Refuge Restrooms
Android app for [Refuge Restrooms](http://www.refugerestrooms.org/)

## Getting started
You can use Android Studio (http://developer.android.com/tools/studio/index.html) to build this project, and it should behave like any other android studio project.

Clone the repo here and the in Android Studio go to File -> New -> Import Project and navigate to your clone.

## Contributing
By all means contribute :) Areas which definitely need work are listed as TODOs below. Feel free to add more.

Please try to stick to the android style guidelines http://source.android.com/source/code-style.html. Fields should start with 'm'
In addition, please put curly brackets round your blocks.

If you push code, please make sure it builds correctly. Feel free to use pull requests so that other contributors can check your code.

## Functionality
- Works best with GPS Location enabled
- Uses [Google Maps Gestures](https://support.google.com/gmm/answer/3139292?hl=en)
- Selecting location marker shows bathroom info and changes navigation to that location
- Navigation icon in the top right gives text directions
- Blue marker = accessible, red marker = not accessible
- When GPS isn't enabled
  - Popup box will recommend turning it on initial app start
  - Navigation icon gives a toast to enable location
  - Selecting marker doesn't give location
  - Currently location defaults to Minneapolis (Fix this!)
  - Only really useful to use search
- If no bathroom within 30(ish?) miles, no results are given by refuge restrooms api, so a toast displays to the user that no bathrooms are nearby
- Max locations shown initially = 20
- Searching with no GPS will move camera to first location found, and shows a toast saying locations were found

## TODO
- [x] Search bar
- [ ] Get and set nearby location when gps is disabled (currently defaults to Minneapolis)
- [ ] Detailed info button like in ios app, Add bathroom rating to info window
- [ ] Indicate red markers vs blue markers for accessibility
- [ ] Make Navigate button floating action button instead?
- [ ] Style
  - [x] Style Feedback Form better
  - [ ] Navigation drawer icons
  - [ ] Style text directions section -- directions.html
- [ ] Better add bathroom section, currently just a webview
- [ ] Contact form without email client
- [ ] Update text directions while on tab
- [ ] Actionbar activity depreciated (HttpClient as well)

## Bugs
- [ ] Screen rotation re-updates map to initial closest location
- [ ] Turning location off after it's been on doesn't grab last known location on app reopen

## Screenshots
![](/app/src/main/res/drawable-hdpi/Screenshots/screen1.png?raw=true)
![](/app/src/main/res/drawable-hdpi/Screenshots/screen2.png?raw=true)
![](/app/src/main/res/drawable-hdpi/Screenshots/screen3.png?raw=true)
![](/app/src/main/res/drawable-hdpi/Screenshots/screen4.png?raw=true)
![](/app/src/main/res/drawable-hdpi/Screenshots/screen5.png?raw=true)