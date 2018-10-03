package view;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import createXml.CreateXML;
import savingFile.FileSaver;
import transformation.TransformationXMLToHTML;

public class MainWindow {

	private JFrame frame;
	private JPanel panel;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JPanel panel_3;
	private JPanel panel_4;
	private JButton btnAddGuest;

	private static final String[] TYPES = new String[] { "Child", "Adult", "Senior" };

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 570, 482);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panel = new JPanel();
		panel.setBounds(12, 12, 556, 32);
		frame.getContentPane().add(panel);

		JLabel lblRegistration = new JLabel("Registration");
		panel.add(lblRegistration);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(12, 56, 544, 95);
		panel_1.setBorder(BorderFactory.createTitledBorder("Buyer info"));
		panel_1.setLayout(null);
		frame.getContentPane().add(panel_1);

		JLabel label = new JLabel("Name:");
		label.setBounds(25, 28, 70, 25);
		panel_1.add(label);

		textField = new JTextField();
		textField.setBounds(110, 28, 130, 25);
		textField.setColumns(10);
		panel_1.add(textField);

		JLabel label_1 = new JLabel("Surname:");
		label_1.setBounds(280, 28, 70, 25);
		panel_1.add(label_1);

		textField_1 = new JTextField();
		textField_1.setBounds(389, 28, 130, 25);
		textField_1.setColumns(10);
		panel_1.add(textField_1);

		JLabel label_2 = new JLabel("Email:");
		label_2.setBounds(25, 60, 70, 25);
		panel_1.add(label_2);

		textField_2 = new JTextField();
		textField_2.setBounds(110, 60, 130, 25);
		textField_2.setColumns(10);
		panel_1.add(textField_2);

		JLabel label_7 = new JLabel("Type:");
		label_7.setBounds(280, 60, 70, 25);
		panel_1.add(label_7);

		JComboBox comboBox = new JComboBox(TYPES);
		comboBox.setBounds(389, 60, 129, 25);
		panel_1.add(comboBox);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(BorderFactory.createTitledBorder("Course info"));
		panel_2.setBounds(12, 160, 544, 95);
		panel_2.setLayout(null);
		frame.getContentPane().add(panel_2);

		JLabel label_3 = new JLabel("Date:");
		label_3.setBounds(25, 28, 70, 25);
		panel_2.add(label_3);

		UtilDateModel model = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		datePicker.setBounds(110, 28, 129, 25);
		panel_2.add(datePicker);

		JLabel label_4 = new JLabel("Time:");
		label_4.setBounds(280, 28, 70, 25);
		panel_2.add(label_4);

		textField_3 = new JTextField();
		textField_3.setBounds(389, 28, 130, 25);
		textField_3.setColumns(10);
		panel_2.add(textField_3);

		JCheckBox chckbxOnlinePayment = new JCheckBox("Online payment");
		chckbxOnlinePayment.setBounds(21, 61, 158, 23);
		panel_2.add(chckbxOnlinePayment);

		panel_3 = new JPanel();
		panel_3.setBorder(BorderFactory.createTitledBorder("Guest info"));
		panel_3.setBounds(12, 267, 544, 127);
		frame.getContentPane().add(panel_3);
		panel_3.setLayout(null);

		JLabel label_5 = new JLabel("Name:");
		label_5.setBounds(25, 28, 70, 25);
		panel_3.add(label_5);

		textField_4 = new JTextField();
		textField_4.setBounds(110, 28, 130, 25);
		textField_4.setColumns(10);
		panel_3.add(textField_4);

		JLabel label_6 = new JLabel("Surname:");
		label_6.setBounds(280, 28, 70, 25);
		panel_3.add(label_6);

		textField_5 = new JTextField();
		textField_5.setBounds(389, 28, 130, 25);
		textField_5.setColumns(10);
		panel_3.add(textField_5);

		JLabel label_8 = new JLabel("Type:");
		label_8.setBounds(25, 60, 70, 25);
		panel_3.add(label_8);

		JComboBox comboBox_1 = new JComboBox(TYPES);
		comboBox_1.setBounds(110, 60, 130, 25);
		panel_3.add(comboBox_1);

		btnAddGuest = new JButton("Add guest");
		btnAddGuest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addGuestRow();
			}
		});
		btnAddGuest.setBounds(412, 92, 106, 25);
		panel_3.add(btnAddGuest);

		panel_4 = new JPanel();
		panel_4.setBounds(12, panel_3.getY() + panel_3.getHeight() + 10, 544, 30);
		panel_4.setLayout(null);
		frame.getContentPane().add(panel_4);
		
		JButton btnGenerateXML = new JButton("Generate XML");
		btnGenerateXML.setSize(120, 25);
		btnGenerateXML.setBounds(371 - btnGenerateXML.getWidth() - 5, 0, btnGenerateXML.getWidth(),
				btnGenerateXML.getHeight());
		btnGenerateXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
								
				String name = textField.getText();
				String surname = textField_1.getText();
				String email = textField_2.getText();
				String type = comboBox.getSelectedItem().toString();
				
				String date = datePicker.getJFormattedTextField().getText();
				String time = textField_3.getText();
				String onlinePay;
				
				if (chckbxOnlinePayment.isSelected()) {
					onlinePay = "true";
				}
				else {
					onlinePay = "false";
				}
				
				String guestName = textField_4.getText();
				String guestSurname = textField_5.getText();
				String guestType = comboBox_1.getSelectedItem().toString();
				
				String xml = CreateXML.generateXML(name, surname, email, type, date, time, onlinePay, guestName, guestSurname, guestType);								
			}
		});
		panel_4.add(btnGenerateXML);

		JButton btnShow = new JButton("Show");
		btnShow.setSize(73, 25);
		btnShow.setBounds(panel_4.getWidth() - btnShow.getWidth() - 2, 0, btnShow.getWidth(), btnShow.getHeight());
		btnShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// File xmlFile = getXMLFile();

				ClassLoader classLoader = new TransformationXMLToHTML().getClass().getClassLoader();
				File xmlFile = new File(classLoader.getResource("ReservationExample.xml").getFile());
				TransformationXMLToHTML.transformXMLToHTML(xmlFile);
			}
		});
		panel_4.add(btnShow);

		JButton btnValidate = new JButton("Validate");
		btnValidate.setSize(93, 25);
		btnValidate.setBounds(btnShow.getX() - btnValidate.getWidth() - 5, 0, btnValidate.getWidth(),
				btnValidate.getHeight());
		btnValidate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// File xmlFile = getXMLFile();

			}
		});
		panel_4.add(btnValidate);
		

	}

	private void addGuestRow() {
		int newY = getNewVerticalPosition();
		updateForm(55 + 15);

		panel_3.add(getGuestNameLabel(newY));
		panel_3.add(getGuestNameTextField(newY));
		panel_3.add(getGuestSurnameLabel(newY));
		panel_3.add(getGuestSurnameTextField(newY));
		panel_3.add(getGuestTypeLabel(newY + 25 + 5));
		panel_3.add(getGuestTypeComboBox(newY + 25 + 5));
	}

	private int getNewVerticalPosition() {
		Component lastRowComp = panel_3.getComponent(panel_3.getComponentCount() - 2);
		return lastRowComp.getY() + lastRowComp.getHeight() + 15;
	}

	private void updateForm(int rowHeight) {
		increasePanelsHeight(rowHeight);
		adjustPositioning(rowHeight);
	}

	private void increasePanelsHeight(int inc) {
		frame.setSize(frame.getWidth(), frame.getHeight() + inc);
		panel_3.setSize(panel_3.getWidth(), panel_3.getHeight() + inc);
	}

	private void adjustPositioning(int inc) {
		panel_4.setBounds(panel_4.getX(), panel_4.getY() + inc, panel_4.getWidth(), panel_4.getHeight());
		btnAddGuest.setBounds(btnAddGuest.getX(), btnAddGuest.getY() + inc, btnAddGuest.getWidth(),
				btnAddGuest.getHeight());
	}

	private JLabel getGuestNameLabel(int yOffset) {
		return getLabel(25, yOffset, "Name:");
	}

	private JTextField getGuestNameTextField(int yOffset) {
		return getTextField(110, yOffset);
	}

	private JLabel getGuestSurnameLabel(int yOffset) {
		return getLabel(280, yOffset, "Surname:");
	}

	private JTextField getGuestSurnameTextField(int yOffset) {
		return getTextField(389, yOffset);
	}

	private JLabel getGuestTypeLabel(int yOffset) {
		return getLabel(25, yOffset, "Type:");
	}

	private JComboBox<String> getGuestTypeComboBox(int yOffset) {
		return getComboBox(110, yOffset);
	}

	private JLabel getLabel(int xOffset, int yOffset, String name) {
		JLabel label = new JLabel(name);
		label.setBounds(xOffset, yOffset, 70, 25);

		return label;
	}

	private JTextField getTextField(int xOffset, int yOffset) {
		JTextField textField = new JTextField();
		textField.setBounds(xOffset, yOffset, 130, 25);

		return textField;
	}

	private JComboBox<String> getComboBox(int xOffset, int yOffset) {
		JComboBox<String> comboBox = new JComboBox<>(TYPES);
		comboBox.setBounds(xOffset, yOffset, 129, 25);

		return comboBox;
	}
}
