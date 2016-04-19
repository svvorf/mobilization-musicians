# Musicians
Test task application for entering Yandex Mobile Development School 2016.

![alt text](https://raw.githubusercontent.com/svvorf/mobilization-musicians/master/publish/showcase.png "Musicians")

#### The app provides minimum required functionality:
* Displaying a list of musicians fetched from URL provided in the test task description
* Showing detailed information about each musician

![alt text](https://raw.githubusercontent.com/svvorf/mobilization-musicians/master/publish/screens.png "Screenshots")

#### Additional features implemented:
##### Multiple screen sizes support: from handsets to tablets with the different layout
The layout is constructed using master-detail flow pattern.
##### Caching API response
The app uses [Realm](https://realm.io) for storing data. An API response is saved after the first successful loading. Updating is implemented via swipe-to-refresh action.
##### Caching images
With Picasso library images are also being cached and not re-downloaded each time.
##### Search
There is search available in the Toolbar.
##### Animations
Animations and transitions is used in the phone layout to smooth opening activities

#### Also
##### Tests
A couple of UI tests have written with the use of Espresso to test basic functionality of the fragments (only for phones)
##### Comments
I've provided most of the classes and some of the methods in the code with comments.

