package SonarUtil;

import java.util.ArrayList;

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;

import robosub.debug;


public class SPI_int implements Runnable {
	Thread t;
	ArrayList<Integer> data = new ArrayList<Integer>();
	SpiChannel chan = SpiChannel.CS0;
	//final GpioController gpio;
	
	public SPI_int(int SPI_con) {
		if(SPI_con == 1)
			chan = SpiChannel.CS1;
	}

	public void getVals() throws Exception{
		//if(gpio == null) gpio = GpioFactory.getInstance();
		final GpioController gpio = GpioFactory.getInstance();
        // Create custom MCP3008 analog gpio provider
        // we must specify which chip select (CS) that that ADC chip is physically connected to.
        final AdcGpioProvider provider = new MCP3008GpioProvider(SpiChannel.CS0,32000000,SpiDevice.DEFAULT_SPI_MODE,false);
        // Provision gpio analog input pins for all channels of the MCP3008.
        // (you don't have to define them all if you only use a subset in your project)
        final GpioPinAnalogInput input= gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, "MyAnalogInput-CH0");
        //ArrayList<Integer> data = new ArrayList<Integer>();
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis()-start < 2000) {
            data.add((int)input.getValue());
        }
	}

	@Override
	public void run() {
		try {
			getVals();
		}catch(Exception e){System.out.println("Error SPI: "+e);
		e.getStackTrace();
		}
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "SPI_int: "+chan);
			t.start();
		}else{
			debug.print("Second instance being made of single thread: "+this.getClass().getName());
		}
	}

	public void shut() {
		/*if(gpio != null)
			gpio.shutdown();*/
	}
	
}
//pi4j, m3008