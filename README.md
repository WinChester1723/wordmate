# WordMate
Provides shortcut functionality to translate any selected text into your desired language quickly.

# Application Usage
When you open WordMate, you will already notice that its interface is quite similar to Google Translate. 
Create your own custom environment using the add language buttons. After pressing the Minimize button, 
WordMate will be running in the background and you can make the program's interface visible at any time by clicking the Tray icon. 
While the program is running in the background, you can select a text and press the CTRL+C button and then press the Home button 
to transmit the selected text to WordMate (You can customize the Home button as you wish. See Customizing section). 
WordMate will translate texts into another language in accordance with your translation environment.

# Building
Since master branch contains library files, you don't need to download any additional SDKs except:
- IDE (Recommended: IntelliJ IDEA)
- OpenJDK 18

# Maven
No additional dependencies nor repositories required for this branch.

# Roadmap
- Deprecate Google Script requirement
- Implement AWS
- Add TTS functionality
- Add STT functionality 

# Known bugs
- Translation issue while fast-typing
- UI buttons does not work properly
- Checkboxes still exist to choose language
- Language detection does not work

# Issues
Feel free to use [issues section](https://github.com/OrkhanGG/wordmate/issues) to ask a question about the problem you encounter. 
