# Colibri
Colibri is an android library for autotesting UI.

Uses UiAutomator and Espresso.

![Colibri](assets/colibri.gif)

## Gradle Dependency

Add it in your root build.gradle at the end of repositories:

````java
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io"}
	}
}
````

Add the dependency:

````java
dependencies {
	androidTestImplementation 'com.github.kernel0x.colibri:1.0.0'
}
````

## How to use

In androidTest create a class inheritable from ColibriTest or in already created class initialize class Colibri.

````java
class SampleColibriTest : ColibriTest() {
    override fun getCondition(): Condition {
        return Condition.Builder()
                .randomInputText(arrayOf("borscht", "vodka", "bear"))
                .pause(Duration(500, TimeUnit.MILLISECONDS))
                .build()
    }

    override fun getStrategy(): Strategy {
        return Monkey()
    }

    @Test
    fun colibriTest() {
        launch()
    }
}
````
OR

````java
class SampleColibriTest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @Test
    fun colibriTest() {
        Colibri.condition(Condition.Builder()
                .randomInputText(arrayOf("borscht", "vodka", "bear"))
                .pause(Duration(500, TimeUnit.MILLISECONDS)).build())
                .strategy(Monkey())
                .launch()
    }
}
````

Everything is simple. Now you can run the test!

## How to works

Colibri runs throughout the app, analyzing UI elements on each screen.

You can set different testing strategies and conditions.   All conditions are available in Condition.Builder()

You can create custom behavior that will be executed at each step (for example for authorization). Example:
````java
.addCustomBehavior(CustomBehavior {
                    if (getCurrentActivity().localClassName.equals(LoginActivity::class.java.canonicalName)) {
                        try {
                            onView(withId(R.id.text_username)).perform(setTextInEditText("mylogin"))
                            onView(withId(R.id.text_password)).perform(setTextInEditText("qwerty"))
                            onView(withId(R.id.button_login)).perform(click())
                            Thread.sleep(Duration.FIVE_SECONDS.valueAsMs)
                        } catch (e: Exception) {}
                    }
                })
````

## Features

* condition configuration
* different behavioral strategies
* customization of behavior
* logging and screenshots

## Try it

Check out the [sample project](/sample) to try it yourself! :wink:

## Releases

Checkout the [Releases](https://github.com/kernel0x/colibri/releases) tab for all release info.
