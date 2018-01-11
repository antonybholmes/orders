package edu.columbia.rdf.orders.app;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jebtk.bioinformatics.ui.Bioinformatics;
import org.jebtk.core.collections.ArrayListCreator;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.io.FileUtils;
import org.jebtk.core.io.Io;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.io.Temp;
import org.jebtk.core.sys.ExternalProcess;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.external.microsoft.Excel;
import org.jebtk.math.matrix.MatrixType;
import org.jebtk.math.ui.external.microsoft.ExcelUI;
import org.jebtk.modern.UI;
import org.jebtk.modern.UIService;
import org.jebtk.modern.button.ModernButtonWidget;
import org.jebtk.modern.clipboard.ClipboardRibbonSection;
import org.jebtk.modern.dataview.ModernDataModel;
import org.jebtk.modern.dialog.MessageDialogType;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.QuickOpenVectorIcon;
import org.jebtk.modern.graphics.icons.QuickSaveVectorIcon;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.help.GuiAppInfo;
import org.jebtk.modern.help.ModernAboutDialog;
import org.jebtk.modern.io.OpenRibbonPanel;
import org.jebtk.modern.io.RecentFilesService;
import org.jebtk.modern.io.SaveAsRibbonPanel;
import org.jebtk.modern.preview.PreviewTablePanel;
import org.jebtk.modern.ribbon.QuickAccessButton;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.ribbon.RibbonMenuItem;
import org.jebtk.modern.widget.tooltip.ModernToolTip;
import org.jebtk.modern.window.ModernRibbonWindow;
import org.jebtk.modern.zoom.ModernStatusZoomSlider;
import org.jebtk.modern.zoom.ZoomModel;

/**
 * Minimal Common Regions.
 *
 * @author Antony Holmes Holmes
 *
 */
public class MainOrdersWindow extends ModernRibbonWindow
    implements ModernClickListener {
  private static final long serialVersionUID = 1L;

  private static final XSSFColor DUPLICATE_COLOR = new XSSFColor(Color.RED);

  private static final Path STOCKS_FILE = PathUtils
      .getPath("res/lab_stocks.txt");

  private Path mFile = null;

  private OpenRibbonPanel mOpenPanel = new OpenRibbonPanel();

  private SaveAsRibbonPanel mSaveAsPanel = new SaveAsRibbonPanel();

  // private PreviewPanel mPreviewPanel = new PreviewTabsPanel();

  private ModernDataModel mModel;

  private Map<String, Item> mInventory;

  private ZoomModel mZoomModel = new ZoomModel();

  private List<String> mExcelPaths;

  private PreviewTablePanel mPreviewPanel;

  public MainOrdersWindow(GuiAppInfo info, List<String> excelPaths)
      throws IOException {
    super(info);

    mExcelPaths = excelPaths;

    setSize(1200, 800);

    createRibbon();

    createUi();

    loadLabStocks();
  }

  public MainOrdersWindow(GuiAppInfo info, List<String> excelPaths, Path file)
      throws IOException, InvalidFormatException {
    this(info, excelPaths);

    open(file);
  }

  public final void createRibbon() {
    RibbonMenuItem menuItem;

    menuItem = new RibbonMenuItem(UI.MENU_OPEN);
    getRibbonMenu().addTabbedMenuItem(menuItem, mOpenPanel);

    menuItem = new RibbonMenuItem(UI.MENU_SAVE_AS);
    getRibbonMenu().addTabbedMenuItem(menuItem, mSaveAsPanel);

    getRibbonMenu().addDefaultItems(getAppInfo());

    getRibbonMenu().addClickListener(this);

    ModernButtonWidget button = new QuickAccessButton(
        UIService.getInstance().loadIcon(QuickOpenVectorIcon.class, 16));
    button.setClickMessage(UI.MENU_OPEN);
    button.setToolTip(new ModernToolTip("Open", "Open a file."));
    button.addClickListener(this);
    getRibbon().addQuickAccessButton(button);

    button = new QuickAccessButton(
        UIService.getInstance().loadIcon(QuickSaveVectorIcon.class, 16));
    button.setClickMessage(UI.MENU_SAVE);
    button.setToolTip(new ModernToolTip("Save", "Save the current file."));
    button.addClickListener(this);
    getRibbon().addQuickAccessButton(button);

    getRibbon().getToolbar("Home").add(new ClipboardRibbonSection(getRibbon()));

    button = new RibbonLargeButton("Create", "Report",
        UIService.getInstance().loadIcon(RunVectorIcon.class, 24));
    button.setToolTip(new ModernToolTip("Create Report",
        "Create a report from the Quartzy report."));
    button.addClickListener(this);
    getRibbon().getToolbar("Home").getSection("Data").add(button);

    getRibbon().setSelectedIndex(1);
  }

  public final void createUi() {
    // setCard(mPreviewPanel);

    getStatusBar().addRight(new ModernStatusZoomSlider(mZoomModel));
  }

  public final void clicked(ModernClickEvent e) {
    if (e.getMessage().equals(UI.MENU_OPEN)
        || e.getMessage().equals(UI.MENU_BROWSE)) {
      try {
        browseForFile();
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals(OpenRibbonPanel.FILE_SELECTED)) {
      try {
        open(mOpenPanel.getSelectedFile());
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals(OpenRibbonPanel.DIRECTORY_SELECTED)) {
      try {
        browseForFile(mOpenPanel.getSelectedDirectory());
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (InvalidFormatException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals(UI.MENU_SAVE)) {
      try {
        export();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals(SaveAsRibbonPanel.DIRECTORY_SELECTED)) {
      try {
        export(mSaveAsPanel.getSelectedDirectory());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals("Create Report")) {
      try {
        createReport();
      } catch (IOException | InvalidFormatException | ParseException e1) {
        e1.printStackTrace();
      }
    } else if (e.getMessage().equals(UI.MENU_ABOUT)) {
      ModernAboutDialog.show(this, getAppInfo());
    } else if (e.getMessage().equals(UI.MENU_EXIT)) {
      close();
    } else {
      // chooseFile(new File(e.getMessage()));
    }
  }

  private void browseForFile() throws IOException, InvalidFormatException {
    browseForFile(RecentFilesService.getInstance().getPwd());
  }

  private void browseForFile(Path pwd)
      throws IOException, InvalidFormatException {
    open(ExcelUI.openExcelFileDialog(this, pwd));
  }

  private void open(Path file) throws IOException, InvalidFormatException {
    if (file == null) {
      return;
    }

    mModel = Bioinformatics.getModel(file,
        true,
        TextUtils.emptyList(),
        0,
        TextUtils.TAB_DELIMITER,
        MatrixType.TEXT);

    // mPreviewPanel.clear();

    String name = PathUtils.toString(file.toAbsolutePath());

    mPreviewPanel = new PreviewTablePanel(mModel, mZoomModel);

    setCard(mPreviewPanel);

    mFile = file;

    setSubTitle(name);

    RecentFilesService.getInstance().add(file);
  }

  private void export() throws IOException {
    export(RecentFilesService.getInstance().getPwd());
  }

  private void export(Path pwd) throws IOException {
    if (mPreviewPanel == null) {
      return;
    }

    ModernDataModel model = mPreviewPanel.getTable().getModel();

    ExcelUI.saveXlsxFileDialog(this, model, pwd);
  }

  private void createReport()
      throws IOException, InvalidFormatException, ParseException {
    if (mFile == null) {
      ModernMessageDialog.createDialog(this,
          getAppInfo().getName(),
          "Please open a Quartzy orders file.",
          MessageDialogType.WARNING);

      return;
    }

    if (mPreviewPanel == null) {
      return;
    }

    ModernDataModel model = mPreviewPanel.getTable().getModel();

    List<Order> orders = new ArrayList<Order>();

    Map<String, XSSFColor> colorMap = new HashMap<String, XSSFColor>();

    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");

    for (int i = 1; i < model.getRowCount(); ++i) {
      String catalog = parseText(model, i, "Catalog");

      String type = parseText(model, i, "Type");

      Color color = Color.BLACK;

      // If not using Lab Stock, change stuff like General Supply to
      // Personal, if we don't know what it is
      if (!type.equals("Lab Stock")) {
        type = "Personal";
      }

      if (type.equals("Lab Stock") && !mInventory.containsKey(catalog)) {
        type = "Personal (mis-classified as Lab Stock)";
        color = Color.BLUE;
      }

      if (!type.equals("Lab Stock") && mInventory.containsKey(catalog)) {
        type = "Lab Stock (mis-classified as " + type + ")";
        color = Color.GREEN;
      }

      // if (!type.equals(checkedType)) {
      // type = "Type wrong: should be " + checkedType;
      // }

      String from = parseText(model, i, "From");

      if (TextUtils.isNullOrEmpty(from)) {
        from = parseText(model, i, "Requested By");
      }

      if (TextUtils.isNullOrEmpty(from)) {
        from = TextUtils.EMPTY_STRING;
      }

      Order order = new Order(parseText(model, i, "Name"),
          parseText(model, i, "Vendor"), catalog, type, type,
          parseText(model, i, "Unit Size"), parseCost(model, i, "Unit Price"),
          parseQuantity(model, i, "Quantity", "Qty"),
          parseCost(model, i, "Total Price"),
          parseCost(model, i, "S&H", "Shipping & Handling"), from,
          sdf.parse(parseText(model, i, "Date Submitted", "Date Requested")));

      orders.add(order);

      colorMap.put(order.getCatalog(), new XSSFColor(color));
    }

    //
    // Find the date range
    //

    Date minDate = null;
    Date maxDate = null;

    for (Order order : orders) {
      if (minDate == null || order.getDate().compareTo(minDate) < 0) {
        minDate = order.getDate();
      }

      if (maxDate == null || order.getDate().compareTo(maxDate) > 0) {
        maxDate = order.getDate();
      }
    }

    //
    // Look for duplicates
    //

    Map<String, Boolean> duplicateMap = new HashMap<String, Boolean>();

    for (Order order : orders) {
      duplicateMap.put(order.getCatalog(),
          duplicateMap.containsKey(order.getCatalog()));
    }

    // List<Order> labStocks = processByType(orders, "Lab Stock");

    // List<Order> personalStocks = processByType(orders, "Personal");

    List<Order> sortedOrders = sortOrdersByVendor(orders);

    // ensure the temp directory exists
    Path tempFile = Temp.generateTempFile("txt");

    DecimalFormat costFormatter = new DecimalFormat("0.00");

    BufferedWriter writer = FileUtils.newBufferedWriter(tempFile);

    try {
      /*
       * writer.write("Order dates\t"); writer.write(sdf.format(minDate));
       * writer.write("\t"); writer.write(sdf.format(maxDate));
       * writer.newLine();
       * //writer.write(TextUtils.repeat(TextUtils.TAB_DELIMITER, 9));
       * writer.newLine();
       */

      // writer.write("Vendor\tCatalog\tName\tUnit Price\tQuantity\tTotal
      // Price\tS&H\tFrom\tType\tVerified Type\tDate Submitted");
      // writer.write("Vendor\tCatalog\tName\tFrom\tType\tUnit
      // Price\tQuantity\tTotal Price\tS&H\tDate Submitted\tApproved By\tDate
      // Ordered\tDate Received\tOrder Processed By\tOrder No.\tTransaction
      // Date\tInvoice No.\tProject No. Charged");
      writer.write(
          "Vendor\tCatalog\tName\tFrom\tType\tUnit Price\tQuantity\tTotal Price\tS&H\tDate Submitted\tApproved By\tDate Ordered\tDate Received");
      writer.newLine();

      //
      // All stocks
      //

      double subTotal = 0;
      double shipping = 0;

      for (Order order : sortedOrders) {
        subTotal += order.getTotal();
        shipping += order.getShipping();
      }

      double total = subTotal + shipping;

      for (Order order : sortedOrders) {
        writeOrder(order, writer);
      }

      writer.newLine();

      writer.write("\t\t\t\t\t\tShipping\t");
      writer.write(costFormatter.format((shipping)));
      writer.newLine();
      writer.write("\t\t\t\t\t\tSub Total\t");
      writer.write(costFormatter.format((subTotal)));
      writer.newLine();
      writer.write("\t\t\t\t\t\tTotal\t");
      writer.write(costFormatter.format(total));
      // writer.write(TextUtils.repeat(TextUtils.TAB_DELIMITER, 9));
      writer.newLine();
      // writer.write(TextUtils.repeat(TextUtils.TAB_DELIMITER, 9));
      // writer.newLine();
      // writer.newLine();
    } finally {
      writer.close();
    }

    // Open with excel

    Path tempExcelFile = createExcelFile(tempFile,
        duplicateMap,
        colorMap,
        minDate,
        maxDate,
        sdf);

    for (String excel : mExcelPaths) {
      String[] commands = { excel,
          PathUtils.toString(tempExcelFile.toAbsolutePath()) };

      try {
        ExternalProcess.run(commands,
            RecentFilesService.getInstance().getPwd());

        // Once it works, exit the loop
        break;
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void writeOrder(Order order, BufferedWriter writer)
      throws IOException {
    // Vendor\tCatalog\tName\tFrom\tType\tQuantity\tUnit Size\tUnit
    // Price\tQuantity\tTotal Price\tS&H\tDate Submitted\t

    writer.write(order.getVendor());
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(order.getCatalog());
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(order.getName());
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(order.getFrom());
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(order.getVerifiedType());
    // writer.write(TextUtils.TAB_DELIMITER);
    // writer.write(order.getUnitSize());
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(Double.toString(order.getUnitPrice()));
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(Double.toString(order.getQuantity()));
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(Double.toString(order.getTotal()));
    writer.write(TextUtils.TAB_DELIMITER);
    writer.write(Double.toString(order.getShipping()));
    writer.write(TextUtils.TAB_DELIMITER);

    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
    String date = sdf.format(order.getDate());

    writer.write(date);

    writer.newLine();
  }

  private Path createExcelFile(Path file,
      Map<String, Boolean> duplicateColorMap,
      Map<String, XSSFColor> colorMap,
      Date minDate,
      Date maxDate,
      SimpleDateFormat sdf) throws IOException, InvalidFormatException {
    Path excelFile = Temp.generateTempFile("xlsx");

    // Open as generic model
    ModernDataModel model = Bioinformatics.getModel(file,
        true,
        TextUtils.emptyList(),
        0,
        TextUtils.TAB_DELIMITER,
        MatrixType.TEXT);

    XSSFWorkbook workbook = new XSSFWorkbook();

    // XSSFFont defaultFont = workbook.getFontAt((short)0);
    // workbook.getFontAt((short)0).setFontName("Arial");

    // defaultFont.setFontName("Arial");
    // defaultFont.setFontHeightInPoints((short)11);

    Sheet sheet = workbook.createSheet("Sheet1");

    // Keep track of how many rows we have created.
    int r = 0;

    // All cells get a default style

    XSSFFont boldFont = workbook.createFont();
    boldFont.setFontHeightInPoints((short) 11);
    boldFont.setFontName("Arial");
    boldFont.setBold(true);

    // Because of some stupid bug in POI, black appears as white
    // in the Excel file, so we pick a color very close to black
    // and use that instead
    // font.setColor(new XSSFColor(new Color(1, 1, 1)));

    XSSFCellStyle dateStyle = workbook.createCellStyle();
    dateStyle.setFont(boldFont);

    XSSFRow row;
    XSSFCell cell;

    //
    // Dates row
    //

    row = (XSSFRow) sheet.createRow(r++);

    cell = row.createCell(0);
    cell.setCellStyle(dateStyle);
    cell.setCellValue("Order dates");
    cell = row.createCell(1);
    cell.setCellStyle(dateStyle);
    cell.setCellValue(sdf.format(minDate));
    cell = row.createCell(2);
    cell.setCellStyle(dateStyle);
    cell.setCellValue(sdf.format(maxDate));

    Excel.createEmptyColumns(10, row);

    // Blank row
    sheet.createRow(r++);

    //
    // Header
    //

    XSSFCellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFont(boldFont);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);

    row = (XSSFRow) sheet.createRow(r++);

    for (int i = 0; i < model.getColumnCount(); ++i) {
      cell = row.createCell(i);

      cell.setCellStyle(headerStyle);
      cell.setCellValue(model.getValueAt(0, i).toString());
    }

    //
    // Rows
    //

    for (int i = 1; i < model.getRowCount(); ++i) {
      row = (XSSFRow) sheet.createRow(r++);

      String catalog = model.getValueAsString(i, 1);

      XSSFFont font = workbook.createFont();
      font.setFontHeightInPoints((short) 11);
      font.setFontName("Arial");

      // Set the color based on whether it is normal, or misclassified
      font.setColor(colorMap.get(catalog));

      // Override any color choice if its a duplicate
      if (catalog != null && duplicateColorMap.containsKey(catalog)
          && duplicateColorMap.get(catalog)) {
        font.setColor(DUPLICATE_COLOR);
      }

      XSSFCellStyle defaultStyle = workbook.createCellStyle();
      defaultStyle.setFont(font);

      // Add a border
      defaultStyle.setBorderBottom(BorderStyle.THIN);
      defaultStyle.setBorderTop(BorderStyle.THIN);
      defaultStyle.setBorderRight(BorderStyle.THIN);
      defaultStyle.setBorderLeft(BorderStyle.THIN);

      for (int j = 0; j < model.getColumnCount(); ++j) {
        cell = row.createCell(j);

        cell.setCellStyle(defaultStyle);

        System.err
            .println("values " + i + " " + j + " " + model.getValueAt(i, j));

        Object o = model.getValueAt(i, j);

        String value = o != null ? o.toString() : TextUtils.EMPTY_STRING;

        value = !value.equals("NaN") ? value : TextUtils.EMPTY_STRING;

        if (!value.equals(TextUtils.EMPTY_STRING)) {
          switch (j) {
          case 5:
          case 6:
          case 7:
          case 8:
            try {
              cell.setCellValue(TextUtils.parseDouble(value));
            } catch (ParseException e) {
              cell.setCellValue(new XSSFRichTextString(value));
            }

            break;
          default:
            cell.setCellValue(new XSSFRichTextString(value));
            break;
          }
        } else {
          cell.setCellValue(new XSSFRichTextString(""));
        }
      }
    }

    for (int i = 0; i < model.getColumnCount(); ++i) {
      sheet.setColumnWidth(i, 256 * 18);
    }

    sheet.setColumnWidth(0, 256 * 30);
    sheet.setColumnWidth(2, 256 * 30);

    /*
     * //Auto size all the columns for (int i = 0; i <
     * sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
     * //sheet.autoSizeColumn(i);
     * 
     * sheet.setColumnWidth(i, 256 * 30); }
     */

    // Now save the excel file

    Excel.writeXlsx(workbook, excelFile);

    model = Bioinformatics.getModel(excelFile,
        true,
        TextUtils.emptyList(),
        0,
        TextUtils.TAB_DELIMITER,
        MatrixType.TEXT);

    // mPreviewPanel.addPreview(PathUtils.toString(file.toAbsolutePath()),
    // new PreviewTablePanel(model, mZoomModel));

    MainOrdersWindow window = new MainOrdersWindow(getAppInfo(),
        this.mExcelPaths, excelFile);

    window.setVisible(true);

    return excelFile;
  }

  private List<Order> processByType(List<Order> orders, String type)
      throws ParseException {
    List<Order> ret = new ArrayList<Order>();

    for (Order order : orders) {
      if (order.getVerifiedType().equals(type)) {
        ret.add(order);
      }
    }

    return sortOrdersByVendor(ret);
  }

  private static double parseCost(ModernDataModel model,
      int row,
      String... names) {
    double cost = 0;

    for (String name : names) {
      String v = model.getValueAsString(row, name);

      if (v != null) {
        cost = parseCost(v);
        break;
      }
    }

    return cost;
  }

  /**
   * Parse a column that may have alternative names since Quartzy has a bad
   * habit of constantly changing the format and names in their export tables.
   * 
   * @param model
   * @param row
   * @param names
   * @return
   */
  private static String parseText(ModernDataModel model,
      int row,
      String... names) {
    for (String name : names) {
      String v = model.getValueAsString(row, name);

      if (v != null) {
        return v;
      }
    }

    return null;
  }

  private static double parseCost(String text) {
    if (text == null) {
      return 0;
    }

    Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");

    Matcher matcher = pattern.matcher(text);

    if (!matcher.find()) {
      return 0;
    }

    try {
      return TextUtils.parseDouble(matcher.group(1));
    } catch (ParseException e) {
      return 0;
    }
  }

  private static double parseQuantity(ModernDataModel model,
      int row,
      String... names) {
    double cost = 0;

    for (String name : names) {
      String v = model.getValueAsString(row, name);

      if (v != null) {
        cost = parseQuantity(v);
        break;
      }
    }

    return cost;
  }

  private static double parseQuantity(String text) {
    return Double.parseDouble(text); // TextUtils.parseDouble(text);
  }

  private List<Order> sortOrdersByVendor(List<Order> orders) {
    Map<String, List<Order>> orderMap = new HashMap<String, List<Order>>();

    for (Order order : orders) {
      if (!orderMap.containsKey(order.getVendor())) {
        orderMap.put(order.getVendor(), new ArrayList<Order>());
      }

      orderMap.get(order.getVendor()).add(order);

      System.err.println(
          order.getCatalog() + " " + orderMap.get(order.getVendor()).size());
    }

    // sort orders

    List<String> sortedVendors = CollectionUtils.sort(orderMap.keySet());

    ArrayList<Order> ret = new ArrayList<Order>();

    for (String vendor : sortedVendors) {
      // Collections.sort(orderMap.get(vendor));

      System.err.println(vendor);

      List<Order> sortedByFrom = sortOrdersByFrom(orderMap.get(vendor));

      for (Order order : sortedByFrom) {
        ret.add(order);
      }
    }

    return ret;
  }

  private Map<String, List<Order>> getVendorMap(List<Order> orders) {
    Map<String, List<Order>> orderMap = DefaultHashMap
        .create(new ArrayListCreator<Order>());

    for (Order order : orders) {
      orderMap.get(order.getVendor()).add(order);

      System.err.println(
          order.getCatalog() + " " + orderMap.get(order.getVendor()).size());
    }

    // sort orders

    for (String vendor : orderMap.keySet()) {
      Collections.sort(orderMap.get(vendor));
    }

    return orderMap;
  }

  private List<Order> sortOrdersByFrom(List<Order> orders) {
    Map<String, List<Order>> fromMap = DefaultHashMap
        .create(new ArrayListCreator<Order>());

    for (Order order : orders) {
      fromMap.get(order.getFrom()).add(order);
    }

    // sort orders

    List<String> sortedFrom = CollectionUtils.sort(fromMap.keySet());

    List<Order> ret = new ArrayList<Order>();

    for (String from : sortedFrom) {
      Collections.sort(fromMap.get(from));

      for (Order order : fromMap.get(from)) {
        ret.add(order);
      }
    }

    return ret;
  }

  private void loadLabStocks() throws IOException {
    mInventory = new HashMap<String, Item>();

    BufferedReader reader = FileUtils.newBufferedReader(STOCKS_FILE);

    String line;
    List<String> tokens;

    try {
      reader.readLine();

      while ((line = reader.readLine()) != null) {
        // System.err.println(line);

        if (Io.isEmptyLine(line)) {
          continue;
        }

        tokens = TextUtils.tabSplit(line);

        Item item = new Item(tokens.get(0), tokens.get(1), tokens.get(2));

        mInventory.put(item.getCatalog(), item);
      }
    } finally {
      reader.close();
    }

  }

  @Override
  public void close() {
    Temp.deleteTempFiles();

    super.close();
  }
}
