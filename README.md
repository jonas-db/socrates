# SoCRATES
SoCRATES is a IntelliJ IDEA plugin to detect test smells in Scala/SBT projects.


# Features
  - Automatically detects and reports on 9 test smells
  - Tests class smells: Global Fixture, Lazy Test
  - Test case smells: Assertion Roulette, Sensitive Equality, Eager Test, Loan Fixture, With Fixture, Fixture Context, Mystery Guest

# Publication
  - Title: Assessing Diffusion and Perception of Test Smells in Scala Projects
  - Authors: Jonas De Bleser, Dario Di Nucci, Coen De Roover
  - Paper: http://soft.vub.ac.be/Publications/2019/vub-soft-tr-19-06.pdf

# Compilation (optional)
  - Clone the project to `~/socrates`
  - Open the project in IntelliJ IDEA
  - Execute the Gradle task `assembly`
  - A succesful compilation results in a .zip file located at `~/socrates/build/distributions/socrates-1.0-SNAPSHOT.zip`

# Installation
  - Open any project in IntelliJ IDEA
  - Go to IntelliJ IDEA -> Preferences -> Plugins -> Install Plugin from Disk...
  - Navigate to `~/socrates/build/distributions/socrates-1.0-SNAPSHOT.zip`
  - Restart IntelliJ IDEA to finalize the installation of the plugin

# Usage
  - Go to Analyze -> Detect Test Smells
  - A dialog with several options appears
        - Java Runtime should point to the `rt.jar` file
        - Ivy2 should point to the ivy2 cache (multiple directories are seperated by `:`)
        - SBT Home should point to the SBT binary
        - SBT Options can be any additional option for SBT (it is recommended to increase the memory for large projects)
    - Pressing the Analyze button will start a background task
    - A succesful analysis results in a report of all test smells for each test class and test case
    - The report can be sorted and filtered to quickly check for test smells

# Development
  - This project is currently not under development any more.
  - Did you find an issue, or do you have pull requests? We will try our best to answer as soon as possible!

# License
  - MIT
