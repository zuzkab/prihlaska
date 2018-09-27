package view;

import java.awt.EventQueue;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class MainWindow {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;

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
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 12, 576, 32);
		frame.getContentPane().add(panel);

		JLabel lblRegistration = new JLabel("Registration");
		panel.add(lblRegistration);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(12, 56, 576, 95);
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

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(BorderFactory.createTitledBorder("Course info"));
		panel_2.setBounds(12, 160, 576, 95);
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
		datePicker.setBounds(110, 28, 130, 25);
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

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(BorderFactory.createTitledBorder("Guest info"));
		panel_3.setBounds(12, 267, 576, 95);
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

		JButton btnAddGuest = new JButton("Add guest");
		btnAddGuest.setBounds(412, 60, 106, 25);
		panel_3.add(btnAddGuest);

		JPanel panel_4 = new JPanel();
		panel_4.setBounds(12, 371, 576, 30);
		panel_4.setLayout(null);
		frame.getContentPane().add(panel_4);

		JButton btnShow = new JButton("Show");
		btnShow.setSize(73, 25);
		btnShow.setBounds(panel_4.getWidth() - btnShow.getWidth() - 2, 0, btnShow.getWidth(), btnShow.getHeight());
		panel_4.add(btnShow);

		JButton btnValidate = new JButton("Validate");
		btnValidate.setSize(93, 25);
		btnValidate.setBounds(btnShow.getX() - btnValidate.getWidth() - 5, 0, btnValidate.getWidth(),
				btnValidate.getHeight());
		panel_4.add(btnValidate);

		JButton btnSave = new JButton("Save");
		btnSave.setSize(68, 25);
		btnSave.setBounds(btnValidate.getX() - btnSave.getWidth() - 5, 0, btnSave.getWidth(), btnSave.getHeight());
		panel_4.add(btnSave);
	}
}
