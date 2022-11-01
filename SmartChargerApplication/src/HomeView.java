import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

public class HomeView {

	// Variables

	public JFrame frame;
	public JLabel labelTemperature;
	public JButton buttonStartCharging;
	public JButton buttonStopCharging;
	public JLabel labelEnergyMeterVoltage;
	public JLabel labelEnergyMeterCurrent;
	public JLabel labelEnergyMeterTPartial;
	public JLabel labelEnergyMeterTotalPower;
	public JLabel labelCable;
	public JButton buttonChangeEvccModuleMode;

	// Functions

	public HomeView() {
		InitializeView();
	}

	public void InitializeView() {
		EventQueue.invokeLater(new Runnable() {
		      @Override
		      public void run() {
						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						int width = (int)screenSize.getWidth();
						int height = (int)screenSize.getHeight();

						// Main Frame

						JFrame frame = new JFrame("Smart Charger Application");
						frame.setSize(width, height);
						frame.setUndecorated(true); // Remove title bar
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
						frame.setVisible(true);
						frame.getContentPane().setBackground(new Color(19, 33, 39));
						frame.setBackground(frame.getContentPane().getBackground());

						// Label Temperature

						labelTemperature = new JLabel("Temperature: ");
						labelTemperature.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelTemperature.setHorizontalAlignment(JLabel.CENTER);
						labelTemperature.setVerticalAlignment(JLabel.CENTER);
						labelTemperature.setBounds(0, 40, width, 40);
						labelTemperature.setForeground(Color.WHITE);
						frame.getContentPane().add(labelTemperature);

						// Label Energy Meter Voltage

						labelEnergyMeterVoltage = new JLabel("Energy Meter Voltage");
						labelEnergyMeterVoltage.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelEnergyMeterVoltage.setHorizontalAlignment(JLabel.CENTER);
						labelEnergyMeterVoltage.setVerticalAlignment(JLabel.CENTER);
						labelEnergyMeterVoltage.setBounds(00, 80, width, 40);
						labelEnergyMeterVoltage.setForeground(Color.WHITE);
						frame.getContentPane().add(labelEnergyMeterVoltage);

						labelEnergyMeterCurrent = new JLabel("Energy Meter Current");
						labelEnergyMeterCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelEnergyMeterCurrent.setHorizontalAlignment(JLabel.CENTER);
						labelEnergyMeterCurrent.setVerticalAlignment(JLabel.CENTER);
						labelEnergyMeterCurrent.setBounds(00, 120, width, 40);
						labelEnergyMeterCurrent.setForeground(Color.WHITE);
						frame.getContentPane().add(labelEnergyMeterCurrent);

						labelEnergyMeterTPartial = new JLabel("Energy Meter TPartial");
						labelEnergyMeterTPartial.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelEnergyMeterTPartial.setHorizontalAlignment(JLabel.CENTER);
						labelEnergyMeterTPartial.setVerticalAlignment(JLabel.CENTER);
						labelEnergyMeterTPartial.setBounds(00, 160, width, 40);
						labelEnergyMeterTPartial.setForeground(Color.WHITE);
						frame.getContentPane().add(labelEnergyMeterTPartial);

						labelEnergyMeterTotalPower = new JLabel("Energy Meter Total Power");
						labelEnergyMeterTotalPower.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelEnergyMeterTotalPower.setHorizontalAlignment(JLabel.CENTER);
						labelEnergyMeterTotalPower.setVerticalAlignment(JLabel.CENTER);
						labelEnergyMeterTotalPower.setBounds(00, 200, width, 40);
						labelEnergyMeterTotalPower.setForeground(Color.WHITE);
						frame.getContentPane().add(labelEnergyMeterTotalPower);

						labelCable = new JLabel("Cable Detection");
						labelCable.setFont(new Font("Lucida Grande", Font.BOLD, 16));
						labelCable.setHorizontalAlignment(JLabel.CENTER);
						labelCable.setVerticalAlignment(JLabel.CENTER);
						labelCable.setBounds(00, 240, width, 40);
						labelCable.setForeground(Color.WHITE);
						frame.getContentPane().add(labelCable);

						frame.getContentPane().setLayout(null);
		  	}
		});
	}
}
