# SoCRATES
`SoCRATES` is an `IntelliJ IDEA` plugin to detect test smells in Scala/SBT projects.

It automatically detects and reports on 6 test smells including 4 variants of General Fixture.

Test class smells               | Test case smells
--------------------------------|------------------
Global Fixture (General Fixture)| Assertion Roulette
Lazy Test                       | Sensitive Equality
|| Eager Test
|| Loan Fixture (General Fixture)
|| With Fixture (General Fixture)
|| Fixture Context (General Fixture)
|| Mystery Guest

## Compilation (optional)
  1. Clone the project to `~/socrates`.
  2. Open the project in IntelliJ IDEA.
  3. Execute the Gradle task `assembly`.
  4. A succesful compilation results in a .zip file located at `~/socrates/build/distributions/socrates-1.0-SNAPSHOT.zip`.

## Installation
  1. Go to `IntelliJ IDEA` -> `Preferences` -> `Plugins` -> `Install Plugin from Disk...`
  2. Navigate to `~/socrates/build/distributions/socrates-1.0-SNAPSHOT.zip`.
  3. Restart `IntelliJ IDEA` to complete the installation.

## Usage
  1. Open any project in `IntelliJ IDEA`.
  2. Go to `Analyze` -> `Detect Test Smells`.
  3. A dialog with the following options appears
       * `Java Runtime` should point to the `rt.jar` file;
       * `Ivy2` should point to the `ivy2 cache` (multiple directories should be seperated by `:`);
       * `SBT Home` should point to the `SBT binary`;
       * any additional `SBT` option can be define in `SBT Options`. For example, we recommend to increase the available memory for large projects.
  4. Press the `Analyze` button to scan the project.
  5. After the analysis, the plugin shows a table with the detected smells for each test class and test case. The table can be sorted and filtered to easily spot test smells.

## Publication
  > **Assessing Diffusion and Perception of Test Smells in Scala Projects**\
  > *Jonas De Bleser, [Dario Di Nucci](http://dardin88.github.io), [Coen De Roover](http://soft.vub.ac.be/~cderoove/)*\
  > Mining Software Repositories 2019 ([MSR 2019](https://conf.researchr.org/home/msr-2019)), Montreal, Canada\
  > Pre-print: http://soft.vub.ac.be/Publications/2019/vub-soft-tr-19-06.pdf

<p align="center"> 
    <a href="http://soft.vub.ac.be/soft/">
        <img src="http://soft.vub.ac.be/soft/sites/default/files/small_soft_logo.png" alt="Soft Logo">
    </a>
</p>
