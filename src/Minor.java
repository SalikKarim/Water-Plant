import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.MonochromeCanvas;
import org.firmata4j.ssd1306.SSD1306;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.*;


public class Minor {
    private static String pumping;

    static final byte I2C0 = 0x3C; // OLED   Display

    public Minor(String pumping) {
        this.pumping = pumping;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var myPort = "/dev/cu.usbserial-0001";
        var device = new FirmataDevice(myPort);
        device.start();
        device.ensureInitializationIsDone();

        I2CDevice i2cObject = device.getI2CDevice((byte) 0x3C); // Use 0x3C for the Grove OLED
        SSD1306 oled = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64); // 128x64 OLED SSD1515
        oled.init();

        var Sensor = device.getPin(14);
        Sensor.setMode(Pin.Mode.ANALOG);

        var Pump = device.getPin(2);
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            public void run() {

                if (Sensor.getValue() > 650)
                {
                    pumping = "Water Pumping, V = "+String.valueOf(Sensor.getValue());
                } else if (Sensor.getValue() <= 650 )
                {
                    pumping = "Water not pumping: V = "+String.valueOf(Sensor.getValue());
                }

                //These are for OLED. These lines display the voltage value on the OLED along with the horizontal line
                oled.getCanvas().clear();
                oled.getCanvas().drawString(0, 0, pumping);
                oled.getCanvas().drawHorizontalLine(0, 17, (int) ((Sensor.getValue()) / 8), MonochromeCanvas.Color.BRIGHT);

                if (Sensor.getValue() > 600) {
                    oled.getCanvas().setTextsize(2);
                    oled.display();
                    try {
                        Pump.setValue(1);
                        sleep(5000);
                        Pump.setValue(0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if (Sensor.getValue()<=650) {
                    oled.getCanvas().setTextsize(2);
                    oled.display();
                    try {
                        Pump.setValue(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void delay(int i) {
            }

        };

        timer.scheduleAtFixedRate(task,0,100);
        {
            Timer displaytime = new Timer();
            TimerTask taskdisplay = new TimerTask() {
                @Override
                public void run() {
                    oled.clear();


                }
            }; timer.scheduleAtFixedRate(taskdisplay,0,10000);



        }



    }

}