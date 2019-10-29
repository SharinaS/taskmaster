# Taskmaster
Taskmaster is an app built with Java on Android Studio that allows a user to keep track of tasks.
* Main page provides navigation to the add tasks and all tasks pages.
* Add Task page allows the user to type in details about a new task, and submit that task

## Contributor
Sharina Stubbs

# Screenshots of App:
### Homepage
![homepage screenshot](screenshots/homepage.jpg)

# Daily Change Log in 2019:
## October 22: Day 1
* Created a new directory and repo to hold the app, named taskmaster. * Created three pages with very basic layout - homepage, add a task, all tasks. 
* The homepage has two buttons, one which allows the user to go to the Add Task view, and the other that allows the user to go to the All Tasks view. 
* Added a task page that allows users to type in a title and a body for their task. 
* Pressing the submit button shows a "Submitted!" label on the task page. 
![homepage screenshot](screenshots/homepage.jpg)

## October 23: Day 2
* Created a Task Detail Page with a title and Lorem Ipsum description
* Created Settings page that allows users to enter their username and hit save.
* The main page was modified to contain three different buttons with hardcoded task titles. When a user taps one of the titles, it goes to the Task Detail page, and the title at the top of the page matches the task title that was tapped on the previous page.
* The homepage contains a button to visit the Settings page. Once the user has entered their username, it displays “{username}’s tasks” above the three task buttons.
![detail screenshot](screenshots/task-detail-page.jpg)

### October 24: Day 3
* Created a Task class. Each task has a title, body and a state.
* Created an enum, which allows the Task state to be new, assigned, in progress or complete. 
* Refactored home page of app to display a RecyclerView to display Task data using a styled fragment.
*  Created a ViewAdapter class that displays data from a list of Tasks. 
* Data that displays on home page includes task and task body, and is from hardcoded Task data. 
* User is able to touch a task in the RecyclerView, and is directed over to a detail page, where the title is rendered correctly (for that task) on the top of that page.
![homepage screenshot](screenshots/homepage-updated.jpg)

### October 25: Day 4
* Modified Add Task form to save the data entered in as a Task in the local SQLite database.
* Fleshed out enum (which works with Task status) to integrate with database.
* Refactored homepage RecyclerView to display all Task entities in database.
* Description and title of a tapped task is displayed on the detail page.
![homepage screenshot](screenshots/homepage_database.jpg)

### October 29: Day 5
* When your application is opened, can make a request to the provided backend server URL to fetch Task data. Task data is desplayed in the RecyclerView.
* Add Task form modified to post entered task data to the server.
* Homepage refreshes Tasks shown after a task is added.


# Resources:
* [Android Buttons](https://developer.android.com/guide/topics/ui/controls/button.html)
* [Android UI Events](https://developer.android.com/guide/topics/ui/ui-events.html)
* [Android SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)
* [The Activity Lifecyle](https://developer.android.com/guide/components/activities/activity-lifecycle)
* [Android Studio User Guide](https://developer.android.com/studio/intro)
* [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview#java)
* [Overview: Saving Data with Room](https://developer.android.com/training/data-storage/room)
* [Enum Types](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html)
* Code Fellows Seattle-Java-401d6 class demo: [Buy Cheap Stuff](https://github.com/codefellows/seattle-java-401d6/tree/master/class-29/BuyCheapStuff)
* [Overview: Saving Data with Room](https://developer.android.com/training/data-storage/room)
* [OkHttp](https://square.github.io/okhttp)
