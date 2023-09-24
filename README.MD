# NoteMusic

---

### What is NoteMusic?

NoteMusic is a plugin based on Bukkit for Minecraft. It supported from 1.8.8 and over than versions on Minecraft Bukkit.

### How do I use it?

First, [download](https://github.com/CoolCLK/NoteMusic/releases) it. Drop file to your **plugins** folder. Run the server and plugin will generate files to configuration. Op or give the permission ```notemusic.*``` than you can use the command ```/notemusic``` or ```/nm``` now.

### How do I import and play music?

Drop the ***midi file*** to **plugins/NoteMusic/mus** folder. For example, I have a file called "***Never Gonna Give You Up.mid***". We can use the command ```/notemusic importmusic Never Gonna Give You Up.mid```, wait while. Then you can use ```/notemusic playmusic Never-Gonna-Give-You-Up``` to play music. And you can hear the music around you now!

### That's cool, but how do I stop or remove music?

You can use the command ```/notemusic stopmusic Never-Gonna-Give-You-Up``` or ```/notemusic stopmusic``` to stop all playing music. In fact, you can drop the midi file to rubbish bin if you imported the music. The imported music will be saved on ***plugins/NoteMusic/music.yml***. Still is that file, you can use command ```/notemusic removemusic Never-Gonna-Give-You-Up```. Then plugin will reload the music list.

### The end

You can use the command ```/notemusic help``` to get more information. If you found any bugs or wanted any new features, you can [create an issues](https://github.com/CoolCLK/NoteMusic/issues/new) to tell us.

---

Powered by CoolCLK. This project obeyed the [MIT License](LICENSE).