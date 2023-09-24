package coolclk.notemusic;

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

public class MidiImporter {
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
                        int channel = sm.getChannel();
                        pattern.add(channel + ":" + key + ":" + velocity + ":" + time);
                    }
                }
            }
        }
        return pattern;
    }

    public static int getMidiTick(File file) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(file);
        return sequence.getResolution();
    }

    public static void importMusic(String fileName) throws InvalidMidiDataException, IOException {
        File midiFile = new File(Main.getProvidingPlugin(Main.class).getDataFolder(), "mus/" + fileName);
        List<String> notesList = getMidiPattern(midiFile);
        int notesSpeed = getMidiTick(midiFile);
        String sectionName = Main.removeFilenameSuffix(fileName).replaceAll(" ", "-");
        Main.music.createSection(sectionName + ".speed");
        Main.music.createSection(sectionName + ".note");
        Main.music.set(sectionName + ".speed", notesSpeed);
        Main.music.set(sectionName + ".note", notesList);
        Main.music.save(new File(Main.getProvidingPlugin(Main.class).getDataFolder(), "music.yml"));
        Main.loadMusic();
    }
}
