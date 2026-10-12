// https://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/
package dnd;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;



import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

/**
 * Demo application panel to display a range slider.
 */
/**
 * Playback controls and time-range editor shown below the playlist.
 *
 * <p>The panel synchronizes text fields and sliders while suppressing feedback
 * loops caused by programmatic updates from the embedded browser.</p>
 */
public class PlaybackControlPanel extends JPanel implements ActionListener {

    private JLabel rangeSliderLabel1 = new JLabel();
    private JLabel rangeSliderValue1 = new JLabel();
    private JLabel rangeSliderLabel2 = new JLabel();
    private JLabel rangeSliderValue2 = new JLabel();
	public JTextField videoBegin;
	public JTextField videoEnd;
	public JTextField videoSet;
    private final JLabel fragmentDuration = new JLabel("Фрагмент: —");
    private final JLabel remainingTime = new JLabel("Осталось: —");
    private boolean flagVideoPosFromBrowser=false;
    public boolean flagFromTable=false;
    public String videosettext="";
    public boolean flag_removeUpdate=false;
    private boolean updatingFromSelection=false;
    
	public JLabel videoDuration;

// перемотка видео
    public TimeRangeSlider rangeSlider = new TimeRangeSlider();
    protected JButton b_previous, b_play, b_pause, b_stop, b_next;
    private boolean playing;
    PlanetariumPlayer player;

    public PlaybackControlPanel(PlanetariumPlayer player_) {
	player=player_;
//        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
/*
        setLayout(new GridBagLayout());
   add(new JButton("hello"));
    // настройка ячейки для текстового поля
    GridBagConstraints textFieldConstraints =
        new GridBagConstraints();
    // заполнение ячейки по горизонтали
    textFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    // просим занять все оставшиеся ячейки
//    textFieldConstraints.gridwidth =  GridBagConstraints.REMAINDER;
    textFieldConstraints.weightx = 0.5f;
    add(new JTextField(10), textFieldConstraints);
//    textFieldConstraints.weightx = 0.1f;
    add(new JButton("cansel"), textFieldConstraints);
*/
JLabel lbFirstName;  

videoBegin = new JTextField(2);
lbFirstName = new JLabel("First name");
videoEnd = new JTextField(2);
videoSet = new JTextField(2);
videoSet.setColumns(6);
videoSet.setHorizontalAlignment(JTextField.CENTER);
videoDuration = new JLabel("0");

// меняем время начала видео в текстовом поле, меняется движок
videoBegin.getDocument().addDocumentListener(new DocumentListener() {
    @Override
    public void changedUpdate(DocumentEvent e) {    System.out.println("videoBegin: changeUpdate");     print();    }
    @Override
    public void removeUpdate(DocumentEvent e) { /*    System.out.println("videoBegin: removeUpdate");  print();   */ }
    @Override
    public void insertUpdate(DocumentEvent e) {     System.out.println("videoBegin: insertUpdate");   print();    }
    private void assistDateText() {
                    Runnable doAssist = new Runnable() {
                        @Override
                        public void run() {
				float foo; try {   foo = Float.parseFloat(videoBegin.getText()); } catch (NumberFormatException e) {   foo = 0; }
	System.out.println("slider:videoBegin.listener: "+foo);
				rangeSlider.setValue((int)foo);
				player.setBeginTime(foo);
                                updateTimeLabels();
				
                        }
                    };
                    SwingUtilities.invokeLater(doAssist);
                }
    public void print() {
        if (updatingFromSelection) return;
	assistDateText();
    }
});

// Время окончания видео. Меняет слайдер
videoEnd.getDocument().addDocumentListener(new DocumentListener() {
    @Override
    public void changedUpdate(DocumentEvent e) {  System.out.println("videoEnd:changeUpdate");      print();    }
    @Override
    public void removeUpdate(DocumentEvent e) {   /*    print();  */  }
    @Override
    public void insertUpdate(DocumentEvent e) {   System.out.println("videoEnd:insertUpdate");     print();    }
    private void assistDateText() {
                    Runnable doAssist = new Runnable() {
                        @Override
                        public void run() {
				float foo; try {   foo = Float.parseFloat(videoEnd.getText()); } catch (NumberFormatException e) {   foo = 0; }
//				rangeSlider.setUpperValue((int)foo);
				System.out.println("slider: videoEnd.listener: "+foo);

				if(!flagVideoPosFromBrowser) { player.setEndTime(foo); } // не работает 01.12.24
                                updateTimeLabels();
//				flagVideoPosFromBrowser=false;

                        }
                    };
                    SwingUtilities.invokeLater(doAssist);
                }
    public void print() {
        if (updatingFromSelection) return;
	assistDateText();
    }
});

// время воспроизведения. текстовое поле. Меняет слайдер
videoSet.getDocument().addDocumentListener(new DocumentListener() {
    @Override
    public void changedUpdate(DocumentEvent e) {       
				float foo; try {   foo = Float.parseFloat(videoSet.getText()); } catch (NumberFormatException ee) {   foo = 0; }
//				System.out.println("videoSet.changedUpdate: "+videoSet.getText()+" "+foo);
    }
    @Override
    public void removeUpdate(DocumentEvent e) {
				float foo; try {   foo = Float.parseFloat(videoSet.getText()); } catch (NumberFormatException ee) {   foo = 0; }
//				System.out.println("videoSet.removeUpdat---: '"+videosettext+"' "+foo);
	flag_removeUpdate=true;
    }
    @Override
    public void insertUpdate(DocumentEvent e) {        
        if (updatingFromSelection) return;
                    Runnable doAssist = new Runnable() {
                        @Override
                        public void run() {
//if(flag_removeUpdate==false) { videosettext=videoSet.getText();}
//else     flag_removeUpdate=false; 
				float foo; try {   foo = Float.parseFloat(videoSet.getText()); } catch (NumberFormatException ee) {   foo = 0; }
				rangeSlider.setPlayheadValue(foo);
//				System.out.println("videoSet.insertUpdate 2: "+videosettext+" "+foo+" flagVideoPosFromBrowser: "+flagVideoPosFromBrowser);
				if(!flagVideoPosFromBrowser) { player.setCurTime(foo); }
				flagVideoPosFromBrowser=false;
                        }
                    };
                    SwingUtilities.invokeLater(doAssist);
   }
});


// https://www.frolov-lib.ru/programming/javasamples/vol5/vol5_6/index.html - описание gridBagLayout
// https://ipsoftware.ru/posts/gridbaglayout/ - здесь дополнения
// https://www.onlinetutorialspoint.com/java/java-swing-borderfactory-example.html - border (с надписями)

        JPanel sets_video = new JPanel();

	GridBagLayout gbl_sets = new GridBagLayout();
	sets_video.setLayout(gbl_sets);
	GridBagConstraints c_sets = new GridBagConstraints();    

//	c_sets.anchor = GridBagConstraints.ABOVE_BASELINE;
	c_sets.fill   = GridBagConstraints.HORIZONTAL;  // заполнение всего размера ячейки по горизонтали
	c_sets.gridheight = 1;
//	c_sets.gridwidth  = 1;
	c_sets.gridx = GridBagConstraints.RELATIVE; 
//	c_sets.gridy = GridBagConstraints.RELATIVE; 
	c_sets.insets = new Insets(2, 10, -2, 10);

	c_sets.ipadx = 300; // задаем ширину ползунка
	c_sets.ipady = 1; // задаем ширину ползунка
	c_sets.gridwidth  = 3; // на ползунок выделяем 3 ячейки, чтобы ниже 3 кнопки умещались в 3 ячейки. Если этого не сделать, кнопка стоп уползает вправо.

	c_sets.gridwidth  = GridBagConstraints.REMAINDER; // последняя в строке. Все что дальше, с новой строки
	c_sets.weightx = 1.0; // занимаем все по ширине
// Перемотка: абсолютный таймлайн и кнопки точного шага.
        JButton seekBackward = new JButton("−");
        JButton seekForward = new JButton("+");
        seekBackward.setToolTipText("Назад на 1 секунду; Ctrl — на 5 секунд");
        seekForward.setToolTipText("Вперёд на 1 секунду; Ctrl — на 5 секунд");
        seekBackward.addActionListener(e -> seekBy(
            (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0 ? -5 : -1));
        seekForward.addActionListener(e -> seekBy(
            (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0 ? 5 : 1));

        JPanel seekRow = new JPanel(new BorderLayout(4, 0));
        seekRow.add(seekBackward, BorderLayout.WEST);
        JPanel seekCenter = new JPanel(new BorderLayout(0, 0));
        seekCenter.add(rangeSlider, BorderLayout.CENTER);
        JPanel currentTimeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        currentTimeRow.add(videoSet);
        seekCenter.add(currentTimeRow, BorderLayout.SOUTH);
        seekRow.add(seekCenter, BorderLayout.CENTER);
        seekRow.add(seekForward, BorderLayout.EAST);
	gbl_sets.setConstraints(seekRow, c_sets);
	sets_video.add(seekRow);

//	c_sets.anchor = GridBagConstraints.BELOW_BASELINE;
	c_sets.ipadx = 260; // задаем ширину ползунка
	c_sets.gridwidth  = GridBagConstraints.REMAINDER; // последняя в строке. Все что дальше, с новой строки

        rangeSlider.setPreferredSize(new Dimension(240, rangeSlider.getPreferredSize().height));
        rangeSlider.setMinimum(0);
        rangeSlider.setMaximum(1000);

        // Add listener to update display.
        rangeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (updatingFromSelection) return;
//		System.out.println("slider:changeListener, flag: "+flagFromTable);
		if(!flagFromTable) {
	                TimeRangeSlider slider = (TimeRangeSlider) e.getSource();
        	        videoBegin.setText(String.valueOf(slider.getValue()));
	                videoEnd.setText(String.valueOf(slider.getUpperValue()));
			flagFromTable=false;
		}
			flagFromTable=false;
            }
        });
        rangeSlider.addPropertyChangeListener("playheadValue", event -> {
            if (updatingFromSelection || flagVideoPosFromBrowser) return;
            videoSet.setText(new DecimalFormat("#0.###")
                .format(rangeSlider.getPlayheadValue()));
            updateTimeLabels();
        });



	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints c = new GridBagConstraints();    

	c.anchor = GridBagConstraints.NORTHWEST; 
	c.fill   = GridBagConstraints.NONE;  
	c.gridheight = 1;
	c.gridwidth  = 1;
	c.gridx = GridBagConstraints.RELATIVE; 
	c.gridy = GridBagConstraints.RELATIVE; 
	c.insets = new Insets(2, 10, 2, 10);
    
	c.ipadx = 32;

	gbl.setConstraints(videoBegin, c);
	add(videoBegin);

	c.ipadx = 300; // задаем ширину ползунка
	c.gridwidth  = 3; // на ползунок выделяем 3 ячейки, чтобы ниже 3 кнопки умещались в 3 ячейки. Если этого не сделать, кнопка стоп уползает вправо.
	gbl.setConstraints(sets_video, c);
        add(sets_video);

	c.ipadx = 32;
//	c.gridwidth  = 1; 
 
	gbl.setConstraints(videoEnd, c);
	add(videoEnd);
	c.gridwidth  = GridBagConstraints.REMAINDER; // последняя в строке. Все что дальше, с новой строки
        JPanel timeInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        timeInfo.add(new JLabel("Длина:"));
        timeInfo.add(videoDuration);
        timeInfo.add(fragmentDuration);
        timeInfo.add(remainingTime);
	gbl.setConstraints(timeInfo, c);
	add(timeInfo);



        ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
        ImageIcon middleButtonIcon = createImageIcon("images/pause.png");
        ImageIcon rightButtonIcon = createImageIcon("images/stop.png");

        b_play = new JButton("", leftButtonIcon);
        b_play.setActionCommand("play");
        b_previous = new JButton("|<");
        b_previous.setActionCommand("previous");

        b_pause = new JButton("", middleButtonIcon);
        b_pause.setMnemonic(KeyEvent.VK_M);
        b_pause.setActionCommand("pause");

        b_stop = new JButton("", rightButtonIcon);
        b_stop.setMnemonic(KeyEvent.VK_E);
        b_stop.setActionCommand("stop");
        b_next = new JButton(">|");
        b_next.setActionCommand("next");
//        b_stop.setEnabled(false);

        //Listen for actions on buttons 1 and 3.
        b_play.addActionListener(this);
        b_previous.addActionListener(this);
        b_stop.addActionListener(this);
        b_pause.addActionListener(this);
        b_next.addActionListener(this);

        b_previous.setToolTipText("Предыдущий трек");
        b_play.setToolTipText("Запустить выбранный трек");
        b_pause.setToolTipText("Пауза");
        b_stop.setToolTipText("Стоп");
        b_next.setToolTipText("Следующий трек");

        //Add Components to this container, using the default FlowLayout.
	c.ipadx = 10;
	c.gridwidth  = 1; 
	c.weightx = 0.0;
	gbl.setConstraints(b_previous, c);
        add(b_previous);
	gbl.setConstraints(b_play, c);
        add(b_play);
	gbl.setConstraints(b_pause, c);
        add(b_pause);
	gbl.setConstraints(b_stop, c);
        add(b_stop);
	c.gridwidth  = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	gbl.setConstraints(b_next, c);
        add(b_next);



/*
    GridBagHelper helper = new GridBagHelper();
    helper.nextCell().gap(4);
    add(new JTextField(4));
    helper.nextCell();
    add(new JLabel("Имя:"), helper.get());
*/
/*
        ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
        ImageIcon middleButtonIcon = createImageIcon("images/pause.png");
        ImageIcon rightButtonIcon = createImageIcon("images/stop.png");

        b_play = new JButton("", leftButtonIcon);
        b_play.setVerticalTextPosition(AbstractButton.CENTER);
        b_play.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        b_play.setMnemonic(KeyEvent.VK_D);
        b_play.setActionCommand("disable");

        b_pause = new JButton("", middleButtonIcon);
        b_pause.setVerticalTextPosition(AbstractButton.BOTTOM);
        b_pause.setHorizontalTextPosition(AbstractButton.CENTER);
        b_pause.setMnemonic(KeyEvent.VK_M);

        b_stop = new JButton("", rightButtonIcon);
        //Use the default text position of CENTER, TRAILING (RIGHT).
        b_stop.setMnemonic(KeyEvent.VK_E);
        b_stop.setActionCommand("enable");
        b_stop.setEnabled(false);

        //Listen for actions on buttons 1 and 3.
        b_play.addActionListener(this);
        b_stop.addActionListener(this);

        b_play.setToolTipText("Click this button to disable the middle button.");
        b_pause.setToolTipText("This middle button does nothing when you click it.");
        b_stop.setToolTipText("Click this button to enable the middle button.");

        //Add Components to this container, using the default FlowLayout.
        add(b_play);
        add(b_pause);
        add(b_stop);


        
        rangeSliderValue1.setHorizontalAlignment(JLabel.LEFT);
        rangeSliderValue2.setHorizontalAlignment(JLabel.LEFT);
        
        rangeSlider.setPreferredSize(new Dimension(240, rangeSlider.getPreferredSize().height));
        rangeSlider.setMinimum(0);
        rangeSlider.setMaximum(10);
        
        // Add listener to update display.
        rangeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                TimeRangeSlider slider = (TimeRangeSlider) e.getSource();
                rangeSliderValue1.setText(String.valueOf(slider.getValue()));
                rangeSliderValue2.setText(String.valueOf(slider.getUpperValue()));
            }
        });



        add(rangeSliderValue1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 0), 0, 0));
        add(rangeSliderValue2, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
        add(rangeSlider      , new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        add(b_play,            new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,  GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 3), 0, 0));
        add(b_pause,           new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 3), 0, 0));
        add(b_stop,            new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,  GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 3, 3), 0, 0));
*/
    }

private void seekBy(int seconds) {
        double target = Math.max(
            rangeSlider.getMinimum(),
            Math.min(rangeSlider.getMaximum(), rangeSlider.getPlayheadValue() + seconds)
        );
        rangeSlider.setPlayheadValue(target);
}

public void setSliderMaximum(float max) {
	System.out.println("PlaybackControlPanel:setSliderMaximum: "+max);
        rangeSlider.setMaximum((int)max);
}

public void setSelectionTimes(float begin, float end, float duration) {
        updatingFromSelection = true;
        try {
            float maximum = duration > 0 ? duration : Math.max(begin, end);
            if (maximum <= 0) maximum = 1;
            videoDuration.setText(duration > 0
                ? new DecimalFormat("#0.###").format(duration)
                : "");
            rangeSlider.setMaximum(Math.max(1, (int)Math.ceil(maximum)));
            rangeSlider.setValue(Math.max(0, (int)begin));
            float displayedEnd = end > 0 ? end : duration;
            rangeSlider.setUpperValue(Math.max(rangeSlider.getValue(), (int)displayedEnd));
            videoBegin.setText(new DecimalFormat("#0.###").format(begin));
            videoEnd.setText(new DecimalFormat("#0.###").format(end));
            videoSet.setText(new DecimalFormat("#0.###").format(begin));
            rangeSlider.setPlayheadValue(Math.max(0, begin));
            updateTimeLabels();
        } finally {
            updatingFromSelection = false;
        }
}
public void setCurTime(float time) {
	flagVideoPosFromBrowser=true; 
        rangeSlider.setPlayheadValue(Math.max(0, time));
	videoSet.setText(String.valueOf(new DecimalFormat("#0.0").format(time)));
        updateTimeLabels();
//	flagVideoPosFromBrowser=false;
}

public void setEndTimeFirst(float time) {
	System.out.println("PlaybackControlPanel:setEndTimeFirst(): "+time);
	flagVideoPosFromBrowser=true; 
	flagFromTable=true;
	System.out.println("PlaybackControlPanel initial end: "+time);
	videoEnd.setText(String.valueOf(time)); // меняем текст в окне перемотки
}

public void setEndTime(float time) {
	System.out.println("PlaybackControlPanel:setEndTime(): "+time);
	flagVideoPosFromBrowser=true; 
	flagFromTable=true;
	float foo=0; try {   foo = Float.parseFloat(videoDuration.getText()); } catch (NumberFormatException e) {   foo = 0; }

	if(time==0.0) { time=foo; rangeSlider.setUpperValue((int)foo); }
	System.out.println("PlaybackControlPanel end: duration="+foo+" value="+videoDuration.getText());
	videoEnd.setText(String.valueOf(time)); // меняем текст в окне перемотки
//	flagVideoPosFromBrowser=false;
}
public void setBeginTime(float time) {
	System.out.println("slider:setBeginTime: "+time);
	flagVideoPosFromBrowser=true; 
	flagFromTable=true;
	videoBegin.setText(String.valueOf(time)); // меняем текст в окне перемотки
//	flagVideoPosFromBrowser=false;
}


public void setDuration(float time) {
	System.out.println("slider.setDuration: "+time);
	videoDuration.setText(String.valueOf(time)); // меняем текст в окне перемотки
        updateTimeLabels();
//	setSliderMaximum(time);
//		setEndTime(time);
}
    
    public void display() {


        // Initialize values.
        rangeSlider.setValue(3);
        rangeSlider.setUpperValue(7);
        
        // Initialize value display.
        rangeSliderValue1.setText(String.valueOf(rangeSlider.getValue()));
        rangeSliderValue2.setText(String.valueOf(rangeSlider.getUpperValue()));
        
        // Create window frame.
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  //      frame.setResizable(false);
        frame.setTitle("Range Slider Demo");
        
        // Set window content and validate.
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.pack();
        
        // Set window location and display.
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** True only while video is actively running, not while paused or stopped. */
    public boolean isPlaying() {
            return playing;
    }

    /** Loads the highlighted editor row and starts it. */
    public void playSelected() {
            if (player.playCurVideo() < 0) return;
            playCurrent();
    }

    private void playCurrent() {
            playing = true;
            b_play.setEnabled(false);
            b_pause.setEnabled(true);
            b_stop.setEnabled(true);
            player.playVideo();
            System.err.println("play");
    }
    public void stop() {
            playing = false;
            b_pause.setEnabled(false);
            b_play.setEnabled(true);
            b_stop.setEnabled(false);
            player.stopVideo();
            System.err.println("stop");
    }

    /** Restores controls when the final playlist item finishes naturally. */
    public void playbackEnded() {
            playing = false;
            b_pause.setEnabled(false);
            b_play.setEnabled(true);
            b_stop.setEnabled(false);
            updateTimeLabels();
    }
    public void pause() {
            playing = false;
            b_pause.setEnabled(false);
            b_play.setEnabled(true);
            b_stop.setEnabled(true);
            player.pauseVideo();
            System.err.println("pause");
    }


    public void actionPerformed(ActionEvent e) {
//	System.out.println("action : "+e.getActionCommand());

        if ("play".equals(e.getActionCommand())) { // play
		playSelected();
        } else if("pause".equals(e.getActionCommand())){// pause
		pause();
        } else if("previous".equals(e.getActionCommand())) {
                if (player.playPreviousVideo() >= 0) playCurrent();
        } else if("next".equals(e.getActionCommand())) {
                if (player.playNextVideo() >= 0) playCurrent();
        } else {               				// stop
		stop();
        }
    }

    private void updateTimeLabels() {
        double begin = parseTime(videoBegin.getText());
        double configuredEnd = parseTime(videoEnd.getText());
        double duration = parseTime(videoDuration.getText());
        double effectiveEnd = configuredEnd > 0 ? configuredEnd : duration;

        if (effectiveEnd > 0) {
            fragmentDuration.setText("Фрагмент: "
                + formatTime(Math.max(0, effectiveEnd - begin)) + " с");
            remainingTime.setText("Осталось: "
                + formatTime(Math.max(0, effectiveEnd - rangeSlider.getPlayheadValue())) + " с");
        } else {
            fragmentDuration.setText("Фрагмент: —");
            remainingTime.setText("Осталось: —");
        }
    }

    private static double parseTime(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try { return Double.parseDouble(text.trim().replace(',', '.')); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private static String formatTime(double time) {
        return new DecimalFormat("#0.0").format(time);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = PlaybackControlPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
