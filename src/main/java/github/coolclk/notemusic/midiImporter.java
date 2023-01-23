package github.coolclk.notemusic;

import org.bukkit.Sound;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class midiImporter {
    public static final int NOTE_ON = 0x90;

    public static List<String> getMidiPattern(File file) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(file);
        List<String> pattern = new ArrayList<>();

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int velocity = sm.getData2();
                        float time = event.getTick();
                        int channel = sm.getChannel(); //通道，但是好像不起作用...?
                        String sound = getSoundNameByChannel(channel);
                        pattern.add(sound + ":" + key + ":" + velocity + ":" + time);
                    }
                }
            }
        }
        return pattern;
    }

    public static String getSoundNameByChannel(int channel) {
        String sound;
        if (channel == 14) sound = "BLOCK_NOTE_BELL";
        else if (channel == 112) sound = "BLOCK_NOTE_CHIME";
        else if (channel >= 24 && channel <= 31) sound = "BLOCK_NOTE_GUITAR";
        else if ((channel >= 114 && channel <= 118) || (channel == 47)) sound = "BLOCK_NOTE_SNARE";
        else if (channel >= 32 && channel <= 39) sound = "BLOCK_NOTE_BASS";
        else if (channel >= 40 && channel <= 55) sound = "BLOCK_NOTE_HARP";
        else if (channel >= 72 && channel <= 79) sound = "BLOCK_NOTE_FLUTE";
        else if (channel == 13) sound = "BLOCK_NOTE_XYLOPHONE";
        else sound = "BLOCK_NOTE_PLING"; ////channel >= 0 && channel <= 23，以及更多的通道
        return sound;
    }

    public static int getMidiTick(File file) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(file);
        return sequence.getResolution();
    }

    public static int importMusic(String fileName) throws InvalidMidiDataException, IOException, InstantiationException, IllegalAccessException {
        File midiFile = new File(main.getProvidingPlugin(main.class).getDataFolder(), "mus/" + fileName);
        List<String> notesList = getMidiPattern(midiFile);
        int notesSpeed = getMidiTick(midiFile);
        String sectionName = main.removeFilenameSuffix(fileName).replaceAll(" ", "-");
        main.music.createSection(sectionName + ".speed");
        main.music.createSection(sectionName + ".note");
        main.music.set(sectionName + ".speed", notesSpeed);
        main.music.set(sectionName + ".note", notesList);
        main.music.save(new File(main.getProvidingPlugin(main.class).getDataFolder(), "music.yml"));
        main.loadMusic();
        return 0;
    }
}
