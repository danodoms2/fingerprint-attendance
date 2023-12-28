/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.Attendance;
import Model.Special_Calendar;
import Utilities.DatabaseUtil;
import Utilities.PaneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileOutputStream;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;

import static Model.Attendance.*;
import static Model.Special_Calendar.getCalendarByUserId;
import Model.Timeoff;
import Utilities.Modal;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

/**
 *
 * @author User
 */
public class ADMIN_AttReportsCTRL implements Initializable{
    @FXML
    private TableView<Attendance> empNameTable, tardinessTable;
    @FXML
    private TableColumn<Attendance, String> empName, tiAM, toAM, tiPM, toPM, remCol, user_id;
    @FXML
    private TableColumn<Attendance, Date> dateCol;
    @FXML
    private Label monthYearLabel, nameLabel, dateTimeLabel, totalLogin, totalAbsent, byPercent, totalLate, totalTardiness;
    @FXML
    private ChoiceBox <String> monthChoiceBox;
    @FXML
    private ChoiceBox<String> yearChoiceBox;
    @FXML
    private TextField searchBar;
    @FXML
    private Button resetBtn, selectBtn, lateBtn, absentBtn, tardBtn, oneDTRBtn, allDTRBtn;
    
    
    DatabaseUtil dbMethods = new DatabaseUtil();
    PaneUtil method = new PaneUtil();
    LocalDate currentDate = LocalDate.now();
    int currentYear = currentDate.getYear();
    Month currentMonth = currentDate.getMonth();
    int currentDay = currentDate.getDayOfMonth();
    String capitalizedMonth = currentMonth.toString().substring(0, 1) + currentMonth.toString().substring(1).toLowerCase();
    
    @Override
    public void initialize(URL location, ResourceBundle rb) {
         try {
        setTable();
        //Tardiness Table
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        tiAM.setCellValueFactory(new PropertyValueFactory<>("timeInAm"));
        toAM.setCellValueFactory(new PropertyValueFactory<>("timeOutAm"));
        tiPM.setCellValueFactory(new PropertyValueFactory<>("timeInPm"));
        toPM.setCellValueFactory(new PropertyValueFactory<>("timeOutPm"));
        remCol.setCellValueFactory(new PropertyValueFactory<>("attendance_status"));
        //Selection Name table
        user_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        empName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
//        dateTimeLabel.setText(String.valueOf(currentMonth)+" "+currentDay+", "+ currentYear);
        ObservableList<String> monthList = FXCollections.observableArrayList();
        monthList.addAll("January", "February", "March", 
                "April", "May", "June", "July", 
                "August", "September", "October", 
                "November", "December");
        monthChoiceBox.setItems(monthList);
        monthChoiceBox.setValue(capitalizedMonth);
        monthChoiceBox.setOnAction(event -> selectMonth());
        yearChoiceBox.setOnAction(event -> selectYear());
        
        ObservableList<String> monthYear = FXCollections.observableArrayList();
                monthYear.addAll("2021","2022");
                for (Attendance attendance : getYearforLabel()){
                        String[] splitItems = attendance.getDate().toString().split("-");
                        monthYear.add(splitItems[0]);
                }
                yearChoiceBox.setItems(monthYear);
                yearChoiceBox.setValue(currentYear+"");
                
          } catch (Exception e) {
        e.printStackTrace();
    }     
    }
    
   private static void mergeCellsHorizontally(XWPFTable table, int row, int startCol, int endCol) {
    System.out.println("Row: " + row + ", StartCol: " + startCol + ", EndCol: " + endCol);

    // Validate table index and row existence
    if (table == null || row < 0 || row >= table.getNumberOfRows()) {
        System.out.println("Invalid table or row index.");
        return;
    }
    
    XWPFTableRow tableRow = table.getRow(row);
    if (tableRow == null) {
        System.out.println("Row does not exist in the table.");
        return;
    }

    // Validate column indices
    if (startCol < 0 || endCol < 0 || startCol >= tableRow.getTableCells().size() || endCol >= tableRow.getTableCells().size()) {
        System.out.println("Invalid column indices.");
        return;
    }

        for (int col = endCol; col >= startCol; col--) {
            XWPFTableCell cell = tableRow.getCell(col);
            if (col == startCol) {
                // Set the grid span for the first cell
                cell.getCTTc().addNewTcPr().addNewGridSpan().setVal(BigInteger.valueOf(endCol - startCol + 1));
        //        System.out.println("Span here---Row: " + row + ", StartCol: " + startCol + ", EndCol: " + endCol);
            } else {
                // Remove the merged cells from the row
                List<CTTc> tcList = tableRow.getCtRow().getTcList();

                if (tcList.size() > 2) {
                    System.out.println("2 " + col + "----" + tcList.size() + "-------------------" + row);
                    tcList.remove(col);
                    System.out.println("Removed ----Row: " + row + ", -------------------------Column: " + col);
                    System.out.println("------------------removed.");
                } else {
                    System.out.println("1 " + col + "----" + tcList.size() + "-------------------" + row);
                }
            }
        }
}

    
    @FXML
    public void generateDTR(ActionEvent event){
        boolean actionIsConfirmed = Modal.actionConfirmed("Generate DTR", "Do you want to generate?", "This will generate the selected employee DTR");
        if (actionIsConfirmed) {
            generateDOCX();
        }
    }
    
    private void generateDOCX() {
    try {
        FileInputStream templateFile = new FileInputStream(new File("DTR.docx"));
        XWPFDocument doc = new XWPFDocument(templateFile);
        String monthYearText = monthYearLabel.getText().toUpperCase();
        String[] monthYear = monthYearText.split(" , ");
        String nameText = nameLabel.getText().toUpperCase();

        String[] name = nameText.split(" ");
        String monthToNum = "";

        int daysInMonth = 0, lateCount = 0, absentCount = 0;
        if (monthYear[0].equals("JANUARY")) { monthToNum = "1"; }
        if (monthYear[0].equals("FEBRUARY")) { monthToNum = "2"; }
        if (monthYear[0].equals("MARCH")) { monthToNum = "3"; }
        if (monthYear[0].equals("APRIL")) { monthToNum = "4"; }
        if (monthYear[0].equals("MAY")) { monthToNum = "5"; }
        if (monthYear[0].equals("JUNE")) { monthToNum = "6"; }
        if (monthYear[0].equals("JULY")) { monthToNum = "7"; }
        if (monthYear[0].equals("AUGUST")) { monthToNum = "8"; }
        if (monthYear[0].equals("SEPTEMBER")) { monthToNum = "9"; }
        if (monthYear[0].equals("OCTOBER")) { monthToNum = "10"; }
        if (monthYear[0].equals("NOVEMBER")) { monthToNum = "11"; }
        if (monthYear[0].equals("DECEMBER")) { monthToNum = "12"; }

        int year = Integer.parseInt(monthYear[1]);
        int month = Integer.parseInt(monthToNum);
        YearMonth yearMonth = YearMonth.of(year, month);
        daysInMonth = yearMonth.lengthOfMonth();

        // Define target texts
        String targetNameText = "Name:";
        String targetMonthText = "For the month of";

        boolean targetNameFound = false;
        boolean targetMonthFound = false;

        for (IBodyElement element : doc.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;

                // Check for "Name:" target text
                if (paragraph.getText().contains(targetNameText)) {
                    // Add a run to the paragraph
                    XWPFRun run = paragraph.createRun();
                    run.setText("       " + nameLabel.getText().toUpperCase());

                    // Can customize the font, size, etc. for the new run if needed
                    run.setFontFamily("Times New Roman");
                    run.setFontSize(12);

                    targetNameFound = true;
                }

                // Check for "For the month of" target text
                if (paragraph.getText().contains(targetMonthText)) {
                    // Add a run to the paragraph
                    XWPFRun run = paragraph.createRun();
                    run.setText(" " + monthYearLabel.getText().toUpperCase());

                    // You can customize the font, size, etc. for the new run if needed
                    run.setFontFamily("Times New Roman");
                    run.setFontSize(12);

                    targetMonthFound = true;
                }
            } else if (element instanceof XWPFTable) {
                ObservableList<Timeoff> dtrTimeoff = Timeoff.getTimeoffDtr();
                ObservableList<Special_Calendar> dtrHoliday = Special_Calendar.getHolidayDtr();
                ObservableList<Attendance> dtrList = Attendance.getDtrForDocx();
                XWPFTable table = (XWPFTable) element;
                int counter = 0;
                List<XWPFTableRow> rows = table.getRows();

                for (int targetRowIndex = 2; targetRowIndex <= 32; targetRowIndex++) {
                    counter++;
                    if(counter>daysInMonth){
                        break;
                    }else{
                        LocalDate localDate = LocalDate.of(year, month, counter);
                        XWPFTableRow targetRow = table.getRow(targetRowIndex);
                        XWPFTableCell targetCell = targetRow.getCell(1);
                        String cellText = targetCell.getText();
                        int rowC =counter +1;
                            
                            
                            for (Attendance attendance : dtrList) {//-----------------Attendance traversal
                                if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                                    mergeCellsHorizontally(table, rowC, 1, 5);
                                    targetCell.setText("SATURDAY                                                                                       ");

                                } else if (localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                                    mergeCellsHorizontally(table, rowC, 1, 5);
                                    targetCell.setText("SUNDAY");

                                } else if ((attendance.getName().toUpperCase()).equals(nameLabel.getText().toUpperCase())
                                        && (attendance.getDtrDate().toUpperCase()).equals(monthYearLabel.getText().toUpperCase())
                                        && (targetRowIndex - 1) == attendance.getDay() && cellText.equals("")) {

                                        targetCell.setText(attendance.getTimeInAm());
                                    targetCell = targetRow.getCell(2);
                                        targetCell.setText(attendance.getTimeOutAm());
                                    targetCell = targetRow.getCell(3);
                                        targetCell.setText(attendance.getTimeInPm());
                                    targetCell = targetRow.getCell(4);
                                        targetCell.setText(attendance.getTimeOutPm());

                                    System.out.println(attendance.getTimeInAm() + ", " + attendance.getTimeOutAm());
                                }
                            } 
                            for (Special_Calendar holiday : dtrHoliday ) {//-----------------Holiday traversal
                                 if(nameLabel.getText().toUpperCase().equals(holiday.getName().toUpperCase())
                                        && holiday.getYear()== year && holiday.getMonth()== month && holiday.getDay()==counter
                                        && localDate.getDayOfWeek() != DayOfWeek.SATURDAY
                                        && localDate.getDayOfWeek() != DayOfWeek.SUNDAY){
                                    System.out.println("-----Timeoff Year: "+holiday.getYear()+" Month: "+holiday.getMonth()+" Day: "+holiday.getDay()+" Type: "+ holiday.getType());
                                    
                                    if(cellText.equals("")){
                                        mergeCellsHorizontally(table, rowC, 1, 5);
                                        targetCell.setText(holiday.getType().toUpperCase());
                                    }
                                }
                             }
                        
                            for (Timeoff timeoff : dtrTimeoff ) {//-----------------Timeoff traversal
                                 if(nameLabel.getText().toUpperCase().equals(timeoff.getName().toUpperCase())
                                         && timeoff.getMonth()== month && timeoff.getDay()==counter
                                         && localDate.getDayOfWeek() != DayOfWeek.SATURDAY
                                         && localDate.getDayOfWeek() != DayOfWeek.SUNDAY){
                                    System.out.println("-----Timeoff Month: "+timeoff.getMonth()+" Day: "+timeoff.getDay()+" Type: "+ timeoff.getType());
                                    
                                    if(cellText.equals("")){
                                        mergeCellsHorizontally(table, rowC, 1, 5);
                                        targetCell.setText(timeoff.getType().toUpperCase());
                                    }
                                }
                             }
                    }
                }
            }
        }

        if (targetNameFound && targetMonthFound) {

            FileOutputStream fileOutputStream = new FileOutputStream(name[1] + "_" + monthYear[0] + "_" + monthYear[1] + ".docx");
            doc.write(fileOutputStream);
            fileOutputStream.close();
            String fileName =name[1] + "_" + monthYear[0] + "_" + monthYear[1] + ".docx";
            System.out.println("-----------------Text added successfully.");
            boolean actionIsConfirmed = Modal.actionConfirmed("Open File", "Do you want to open the File?", "This action will open "+name[1] + "_" + monthYear[0] + "_" + monthYear[1]+" file.");
                if (actionIsConfirmed) {
                    File file = new File(fileName);
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(file);
                }
        } else {
            System.out.println("-----------------Target text(s) not found.");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}



    
   public void selectYear() {
        try {
            String selectedMonth = (String) monthChoiceBox.getSelectionModel().getSelectedItem();
            String selectedYear = (String) yearChoiceBox.getSelectionModel().getSelectedItem();
            monthYearLabel.setText(selectedMonth + " , " + selectedYear);

            if (nameLabel.getText() != null) {
                Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
                showAttTable(selectedItem.getId());
            }
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace(); // You can replace this with proper logging
            // You can also show a user-friendly error message if needed
        }
    }
    public void selectMonth(){
         try {
            String selectedMonth = (String) monthChoiceBox.getSelectionModel().getSelectedItem();
            String selectedYear = (String) yearChoiceBox.getSelectionModel().getSelectedItem();
            monthYearLabel.setText(selectedMonth + " , " + selectedYear);

            if (nameLabel.getText() != null) {
                Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
                showAttTable(selectedItem.getId());
            }
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace(); // You can replace this with proper logging
            // You can also show a user-friendly error message if needed
        }
    }
    
    public void setTable(){
        
        empNameTable.setItems(Attendance.getEmpName());
        monthChoiceBox.setValue(capitalizedMonth);
        yearChoiceBox.setValue(currentYear+"");
        searchBar.setText("");
        nameLabel.setText("");
        totalLogin.setText("");
        totalTardiness.setText("");
        byPercent.setText("0%");
        totalLate.setText("");
        totalAbsent.setText("");
        searchBar.setPromptText("Search name...");
        tardinessTable.setItems(null);
        monthChoiceBox.setOnAction(event -> selectMonth());
        yearChoiceBox.setOnAction(event -> selectYear());
    }
    @FXML
    private void filterBySearchBar(KeyEvent event) {
        ObservableList<Attendance> filteredData = FXCollections.observableArrayList();
        String keyword = searchBar.getText().toLowerCase(); 
        
            for (Attendance attendance : getEmpName()) {   
                    if ((attendance.getName().toLowerCase()).contains(keyword)){
                        filteredData.add(attendance);
                    }
                }
        empNameTable.setItems(filteredData);
    }
    @FXML
    private void selectEMployee(MouseEvent event) {
        Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
        showAttTable(selectedItem.getId());
        if (selectedItem != null) {
            nameLabel.setText(selectedItem.getName());
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        }else{
            totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalAbsent.setText("");
            totalTardiness.setText("");
            searchBar.setPromptText("Search name...");
            tardinessTable.setItems(null);
        }
    }
    @FXML
    private void selectEMployeeLate(MouseEvent event) {
        Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
        showAttTableLate(selectedItem.getId());
        if (selectedItem != null) {
            nameLabel.setText(selectedItem.getName());
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        }else{
            totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalAbsent.setText("");
            totalTardiness.setText("");
            searchBar.setPromptText("Search name...");
            tardinessTable.setItems(null);
        }
    }
     @FXML
    private void selectEMployeeAbsent(MouseEvent event) {
        Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
        showAttTableAbsent(selectedItem.getId());
        if (selectedItem != null) {
            nameLabel.setText(selectedItem.getName());
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        }else{
            totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalAbsent.setText("");
            totalTardiness.setText("");
            searchBar.setPromptText("Search name...");
            tardinessTable.setItems(null);
        }
    }
    
     public void showAttTable(int user_id){
         totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalTardiness.setText("");
            totalAbsent.setText("");
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        ObservableList<Attendance> filteredData = FXCollections.observableArrayList();
                List<String> dayHolder = new LinkedList<>();
                List<String> holidayHolder = new LinkedList<>();
        String selectedYear = (String) yearChoiceBox.getSelectionModel().getSelectedItem();
        String[] dateOnCTRL = monthYearLabel.getText().split(" ");
        String selectedName = nameLabel.getText();
        String monthToNum = "",converted ="";
        // Count of days that are not Saturday or Sunday
        int workingDaysInMonth = 0, SpecialDays=0;
        
        
        double tardiness;
        int daysInMonth=0, lateCount = 0, absentCount = 0;
                if(dateOnCTRL[0].equals("January")){monthToNum="1";}if(dateOnCTRL[0].equals("February")){monthToNum="2";}
                if(dateOnCTRL[0].equals("March")){monthToNum="3";}if(dateOnCTRL[0].equals("April")){monthToNum="4";}
                if(dateOnCTRL[0].equals("May")){monthToNum="5";}if(dateOnCTRL[0].equals("June")){monthToNum="6";}
                if(dateOnCTRL[0].equals("July")){monthToNum="7";}if(dateOnCTRL[0].equals("August")){monthToNum="8";}
                if(dateOnCTRL[0].equals("September")){monthToNum="9";}if(dateOnCTRL[0].equals("October")){monthToNum="10";}
                if(dateOnCTRL[0].equals("November")){monthToNum="11";}if(dateOnCTRL[0].equals("December")){monthToNum="12";}
                 
                int year= Integer.parseInt(dateOnCTRL[2]);
                int month = Integer.parseInt(monthToNum);
                YearMonth yearMonth = YearMonth.of(year, month);
                daysInMonth = yearMonth.lengthOfMonth();
                
                //Checking if there's the Special calendar
                    for (Special_Calendar sCalendar : getCalendarByUserId(user_id)) {
                        String[] dateOnModel = sCalendar.getStartDate().toString().split("-");
                        int total = (int) sCalendar.getTotal();
                        if (dateOnModel[1].equals(monthToNum)) {
                            int toNum = Integer.parseInt(dateOnModel[2]);
                            for (int l = 0; l < total; l++) {
                                if (toNum < daysInMonth && toNum >= 1) {
                                    LocalDate specialDate = LocalDate.of(year, month, toNum);
                                    if (specialDate.getDayOfWeek() != DayOfWeek.SATURDAY && specialDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                        SpecialDays++;
                                    }
                                    holidayHolder.add(toNum + "");
                                }
                                toNum++;
                            }
                        }
                    }
                    
                for (int k = 1; k <= daysInMonth; k++) {
                    LocalDate localDate1 = LocalDate.of(year, month, k);
                // Check if the day is not Saturday (DayOfWeek.SATURDAY) or Sunday (DayOfWeek.SUNDAY)
                    if (localDate1.getDayOfWeek() != DayOfWeek.SATURDAY && localDate1.getDayOfWeek() != DayOfWeek.SUNDAY) {
                        workingDaysInMonth++;
                    }
                }
                
                    System.out.println(daysInMonth);
                    System.out.println(Arrays.toString(holidayHolder.toArray()));
                for (Attendance attendance : getAttendancebyLate(user_id)){
                    System.out.println(attendance.getDate()+", "+attendance.getTimeInAm()+", "+attendance.getTimeOutAm()+", "+attendance.getTimeInPm()+", "+attendance.getTimeOutPm());
                    String[] dateOnModel = attendance.getDate().toString().split("-");
                    if(dateOnModel[1].equals(monthToNum)){
                        dayHolder.add(dateOnModel[2]);
                    }
                }
                for(int i =1; i<=daysInMonth; i++){
                    LocalDate localDate = LocalDate.of(year, month, i);
                    if(holidayHolder.contains(String.valueOf(i))){
                        // Skip Holidays and TimeOFFs
                    }else
                    if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        // Skip Saturday and Sunday
                    }else{
                        boolean dayChecker = false;
                        for(int j =0; j<dayHolder.size(); j++){
                            int amDay = Integer.parseInt(dayHolder.get(j));
                                if(i==amDay){
                                dayChecker=true;
                            }
                        }
                        if(dayChecker==true){
                            for (Attendance attendance : getAttendancebyLate(user_id)){

                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Late")){
                                    filteredData.add(attendance);
                                     lateCount++;
                                    break;
                                }
                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Absent")){
                                    filteredData.add(attendance);
                                     absentCount++;
                                    break;
                                }
                            }
                        }
                        if(dayChecker==false){
                            for (Attendance attendance : getAttendancebyLate(user_id)){
                                attendance.setDate(java.sql.Date.valueOf(localDate));
                                attendance.setTimeInAm("    --");
                                attendance.setTimeOutAm("    --");
                                attendance.setTimeInPm("    --");
                                attendance.setTimeOutPm("    --");
                                attendance.setAttendance_status("Absent");
                                filteredData.add(attendance);
                                absentCount++;
                                break;
                            }
                        }
                    }
                }
                int roundedTardiness=0;
            if (lateCount != 0 || absentCount != 0) {
                tardiness = ((double) (lateCount + absentCount) / (double) 10) * 100;
                if (tardiness > 100.0) {
                    tardiness = 100.0;  // Cap the tardiness at 100%
                }
                converted = String.format("%.1f", tardiness);
                roundedTardiness = (int) Math.round(Double.parseDouble(converted));
                workingDaysInMonth = workingDaysInMonth-SpecialDays;
                totalLogin.setText(workingDaysInMonth + "");  
                totalLate.setText(lateCount + "");
                totalAbsent.setText(absentCount + "");
                int totalTard = (int)lateCount + absentCount;
                totalTardiness.setText(totalTard+ "");
            } else {
                converted = "0";
            }
            tardinessTable.setItems(filteredData);
            byPercent.setText(roundedTardiness + "%"); 
     }   
      public void showAttTableLate(int user_id){
         totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalTardiness.setText("");
            totalAbsent.setText("");
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        ObservableList<Attendance> filteredData = FXCollections.observableArrayList();
                List<String> dayHolder = new LinkedList<>();
                List<String> holidayHolder = new LinkedList<>();
        String selectedYear = (String) yearChoiceBox.getSelectionModel().getSelectedItem();
        String[] dateOnCTRL = monthYearLabel.getText().split(" ");
        String selectedName = nameLabel.getText();
        String monthToNum = "",converted ="";
        // Count of days that are not Saturday or Sunday
        int workingDaysInMonth = 0, SpecialDays=0;
        
        
        double tardiness;
        int daysInMonth=0, lateCount = 0, absentCount = 0;
                if(dateOnCTRL[0].equals("January")){monthToNum="1";}if(dateOnCTRL[0].equals("February")){monthToNum="2";}
                if(dateOnCTRL[0].equals("March")){monthToNum="3";}if(dateOnCTRL[0].equals("April")){monthToNum="4";}
                if(dateOnCTRL[0].equals("May")){monthToNum="5";}if(dateOnCTRL[0].equals("June")){monthToNum="6";}
                if(dateOnCTRL[0].equals("July")){monthToNum="7";}if(dateOnCTRL[0].equals("August")){monthToNum="8";}
                if(dateOnCTRL[0].equals("September")){monthToNum="9";}if(dateOnCTRL[0].equals("October")){monthToNum="10";}
                if(dateOnCTRL[0].equals("November")){monthToNum="11";}if(dateOnCTRL[0].equals("December")){monthToNum="12";}
                 
        int year= Integer.parseInt(dateOnCTRL[2]);
                int month = Integer.parseInt(monthToNum);
                YearMonth yearMonth = YearMonth.of(year, month);
                daysInMonth = yearMonth.lengthOfMonth();
                
                //Checking if there's the Special calendar
                    for (Special_Calendar sCalendar : getCalendarByUserId(user_id)) {
                        String[] dateOnModel = sCalendar.getStartDate().toString().split("-");
                        int total = (int) sCalendar.getTotal();
                        if (dateOnModel[1].equals(monthToNum)) {
                            int toNum = Integer.parseInt(dateOnModel[2]);
                            for (int l = 0; l < total; l++) {
                                if (toNum < daysInMonth && toNum >= 1) {
                                    LocalDate specialDate = LocalDate.of(year, month, toNum);
                                    if (specialDate.getDayOfWeek() != DayOfWeek.SATURDAY && specialDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                        SpecialDays++;
                                    }
                                    holidayHolder.add(toNum + "");
                                }
                                toNum++;
                            }
                        }
                    }
 // -------------------------naay bug, pag saturday or sunday ang holiday. magDouble ang minus sa working days.
                for (int k = 1; k <= daysInMonth; k++) {
                    LocalDate localDate1 = LocalDate.of(year, month, k);
                // Check if the day is not Saturday (DayOfWeek.SATURDAY) or Sunday (DayOfWeek.SUNDAY)
                    if (localDate1.getDayOfWeek() != DayOfWeek.SATURDAY && localDate1.getDayOfWeek() != DayOfWeek.SUNDAY) {
                        workingDaysInMonth++;
                    }
                }
                
                    System.out.println(daysInMonth);
                    System.out.println(Arrays.toString(holidayHolder.toArray()));
                for (Attendance attendance : getAttendancebyLate(user_id)){
                    System.out.println(attendance.getDate()+", "+attendance.getTimeInAm()+", "+attendance.getTimeOutAm()+", "+attendance.getTimeInPm()+", "+attendance.getTimeOutPm());
                    String[] dateOnModel = attendance.getDate().toString().split("-");
                    if(dateOnModel[1].equals(monthToNum)){
                        dayHolder.add(dateOnModel[2]);
                    }
                }
                for(int i =1; i<=daysInMonth; i++){
                    LocalDate localDate = LocalDate.of(year, month, i);
                    if(holidayHolder.contains(String.valueOf(i))){
                        // Skip Holidays and TimeOFFs
                    }else
                    if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        // Skip Saturday and Sunday
                    }else{
                        boolean dayChecker = false;
                        for(int j =0; j<dayHolder.size(); j++){
                            int amDay = Integer.parseInt(dayHolder.get(j));
                                if(i==amDay){
                                dayChecker=true;
                            }
                        }
                        if(dayChecker==true){
                            for (Attendance attendance : getAttendancebyLate(user_id)){

                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Late")){
                                    filteredData.add(attendance);
                                     lateCount++;
                                    break;
                                }
                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Absent")){
//                                    filteredData.add(attendance);
                                     absentCount++;
                                    break;
                                }
                            }
                        }
                        if(dayChecker==false){
                            for (Attendance attendance : getAttendancebyLate(user_id)){
//                                attendance.setDate(java.sql.Date.valueOf(localDate));
//                                attendance.setTimeInAm("    --");
//                                attendance.setTimeOutAm("    --");
//                                attendance.setTimeInPm("    --");
//                                attendance.setTimeOutPm("    --");
//                                attendance.setAttendance_status("Absent");
//                                filteredData.add(attendance);
                                absentCount++;
                                break;
                            }
                        }
                    }
                }
                int roundedTardiness=0;
            if (lateCount != 0 || absentCount != 0) {
                tardiness = ((double) (lateCount + absentCount) / (double) 10) * 100;
                if (tardiness > 100.0) {
                    tardiness = 100.0;  // Cap the tardiness at 100%
                }
                converted = String.format("%.1f", tardiness);
                roundedTardiness = (int) Math.round(Double.parseDouble(converted));
                workingDaysInMonth = workingDaysInMonth-SpecialDays;
                totalLogin.setText(workingDaysInMonth + "");  
                totalLate.setText(lateCount + "");
                totalAbsent.setText(absentCount + "");
                int totalTard = (int)lateCount + absentCount;
                totalTardiness.setText(totalTard+ "");
            } else {
                converted = "0";
            }
            tardinessTable.setItems(filteredData);
            byPercent.setText(roundedTardiness + "%"); 
     }  
     public void showAttTableAbsent(int user_id){
         totalLogin.setText("");
            byPercent.setText("0%");
            totalLate.setText("");
            totalTardiness.setText("");
            totalAbsent.setText("");
            searchBar.setText("");
            searchBar.setPromptText("Search name...");
        ObservableList<Attendance> filteredData = FXCollections.observableArrayList();
                List<String> dayHolder = new LinkedList<>();
                List<String> holidayHolder = new LinkedList<>();
        String selectedYear = (String) yearChoiceBox.getSelectionModel().getSelectedItem();
        String[] dateOnCTRL = monthYearLabel.getText().split(" ");
        String selectedName = nameLabel.getText();
        String monthToNum = "",converted ="";
        // Count of days that are not Saturday or Sunday
        int workingDaysInMonth = 0, SpecialDays=0;
        
        
        double tardiness;
        int daysInMonth=0, lateCount = 0, absentCount = 0;
                if(dateOnCTRL[0].equals("January")){monthToNum="1";}if(dateOnCTRL[0].equals("February")){monthToNum="2";}
                if(dateOnCTRL[0].equals("March")){monthToNum="3";}if(dateOnCTRL[0].equals("April")){monthToNum="4";}
                if(dateOnCTRL[0].equals("May")){monthToNum="5";}if(dateOnCTRL[0].equals("June")){monthToNum="6";}
                if(dateOnCTRL[0].equals("July")){monthToNum="7";}if(dateOnCTRL[0].equals("August")){monthToNum="8";}
                if(dateOnCTRL[0].equals("September")){monthToNum="9";}if(dateOnCTRL[0].equals("October")){monthToNum="10";}
                if(dateOnCTRL[0].equals("November")){monthToNum="11";}if(dateOnCTRL[0].equals("December")){monthToNum="12";}
                 
        int year= Integer.parseInt(dateOnCTRL[2]);
                int month = Integer.parseInt(monthToNum);
                YearMonth yearMonth = YearMonth.of(year, month);
                daysInMonth = yearMonth.lengthOfMonth();
                
                //Checking if there's the Special calendar
                    for (Special_Calendar sCalendar : getCalendarByUserId(user_id)) {
                        String[] dateOnModel = sCalendar.getStartDate().toString().split("-");
                        int total = (int) sCalendar.getTotal();
                        if (dateOnModel[1].equals(monthToNum)) {
                            int toNum = Integer.parseInt(dateOnModel[2]);
                            for (int l = 0; l < total; l++) {
                                if (toNum < daysInMonth && toNum >= 1) {
                                    LocalDate specialDate = LocalDate.of(year, month, toNum);
                                    if (specialDate.getDayOfWeek() != DayOfWeek.SATURDAY && specialDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                        SpecialDays++;
                                    }
                                    holidayHolder.add(toNum + "");
                                }
                                toNum++;
                            }
                        }
                    }
 // -------------------------naay bug, pag saturday or sunday ang holiday. magDouble ang minus sa working days.
                for (int k = 1; k <= daysInMonth; k++) {
                    LocalDate localDate1 = LocalDate.of(year, month, k);
                // Check if the day is not Saturday (DayOfWeek.SATURDAY) or Sunday (DayOfWeek.SUNDAY)
                    if (localDate1.getDayOfWeek() != DayOfWeek.SATURDAY && localDate1.getDayOfWeek() != DayOfWeek.SUNDAY) {
                        workingDaysInMonth++;
                    }
                }
                
                    System.out.println(daysInMonth);
                    System.out.println(Arrays.toString(holidayHolder.toArray()));
                for (Attendance attendance : getAttendancebyLate(user_id)){
                    System.out.println(attendance.getDate()+", "+attendance.getTimeInAm()+", "+attendance.getTimeOutAm()+", "+attendance.getTimeInPm()+", "+attendance.getTimeOutPm());
                    String[] dateOnModel = attendance.getDate().toString().split("-");
                    if(dateOnModel[1].equals(monthToNum)){
                        dayHolder.add(dateOnModel[2]);
                    }
                }
                for(int i =1; i<=daysInMonth; i++){
                    LocalDate localDate = LocalDate.of(year, month, i);
                    if(holidayHolder.contains(String.valueOf(i))){
                        // Skip Holidays and TimeOFFs
                    }else
                    if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        // Skip Saturday and Sunday
                    }else{
                        boolean dayChecker = false;
                        for(int j =0; j<dayHolder.size(); j++){
                            int amDay = Integer.parseInt(dayHolder.get(j));
                                if(i==amDay){
                                dayChecker=true;
                            }
                        }
                        if(dayChecker==true){
                            for (Attendance attendance : getAttendancebyLate(user_id)){

                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Late")){
//                                    filteredData.add(attendance);
                                     lateCount++;
                                    break;
                                }
                                if(attendance.getDate().equals(java.sql.Date.valueOf(localDate))
                                        && attendance.getAttendance_status().equals("Absent")){
                                    filteredData.add(attendance);
                                     absentCount++;
                                    break;
                                }
                            }
                        }
                        if(dayChecker==false){
                            for (Attendance attendance : getAttendancebyLate(user_id)){
                                attendance.setDate(java.sql.Date.valueOf(localDate));
                                attendance.setTimeInAm("    --");
                                attendance.setTimeOutAm("    --");
                                attendance.setTimeInPm("    --");
                                attendance.setTimeOutPm("    --");
                                attendance.setAttendance_status("Absent");
                                filteredData.add(attendance);
                                absentCount++;
                                break;
                            }
                        }
                    }
                }
                int roundedTardiness=0;
            if (lateCount != 0 || absentCount != 0) {
                tardiness = ((double) (lateCount + absentCount) / (double) 10) * 100;
                if (tardiness > 100.0) {
                    tardiness = 100.0;  // Cap the tardiness at 100%
                }
                converted = String.format("%.1f", tardiness);
                roundedTardiness = (int) Math.round(Double.parseDouble(converted));
                workingDaysInMonth = workingDaysInMonth-SpecialDays;
                totalLogin.setText(workingDaysInMonth + "");  
                totalLate.setText(lateCount + "");
                totalAbsent.setText(absentCount + "");
                int totalTard = (int)lateCount + absentCount;
                totalTardiness.setText(totalTard+ "");
            } else {
                converted = "0";
            }
            tardinessTable.setItems(filteredData);
            byPercent.setText(roundedTardiness + "%"); 
     }
//   
    
    @FXML
    private void resetSelectedItems(ActionEvent event){
        setTable();
    }
    
//    @FXML
//     public void showOnLabels() {
//        Attendance selectedItem = empNameTable.getSelectionModel().getSelectedItem();
//        if (selectedItem != null) {
//            nameLabel.setText(selectedItem.getName());
//        }
//    }

    private void removeTableCells(XWPFTableRow targetRow, int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void removeCells(XWPFTableRow targetRow, int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void removeCellContent(XWPFTableRow targetRow, int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

