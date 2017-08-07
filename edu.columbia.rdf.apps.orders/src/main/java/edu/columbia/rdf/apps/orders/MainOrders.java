package edu.columbia.rdf.apps.orders;



import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import org.abh.common.AppService;
import org.abh.common.io.Io;
import org.abh.common.io.PathUtils;
import org.abh.common.ui.UI;
import org.abh.common.ui.theme.ThemeService;




public class MainOrders {
	private static final Path EXCEL_PATHS_FILE = 
			PathUtils.getPath("excel_paths.txt");

	public static final void main(String[] args) throws FontFormatException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		AppService.getInstance().setAppInfo("orders");
		
		ThemeService.getInstance().setTheme();
		
		
		// Read the excel paths
		
		List<String> excelPaths = Io.getLines(EXCEL_PATHS_FILE);
		

		OrdersInfo info = new OrdersInfo();
		
		JFrame window = new MainOrdersWindow(info, excelPaths);

		UI.centerWindowToScreen(window);

		window.setVisible(true);
	}
}
