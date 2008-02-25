package com.dexels.navajo.tipi.swingclient.components;

import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.dexels.navajo.document.*;

import java.util.*;
import javax.swing.border.*;


public  class PropertyCellEditor
    implements TableCellEditor, ListSelectionListener {

  private Property myProperty = null;

  private final ArrayList myListeners = new ArrayList();

  private JComponent lastComponent = null;
  private MessageTable myTable;

  private int lastRow = -1;
  private int lastColumn = -1;
  private boolean wasSelected = false;
  private Property copy;
  private JButton rowButton = new JButton();
  private boolean isChangingSelection = false;
  private int lastSelectedRow = 0;

  public PropertyCellEditor() {}

  public PropertyCellEditor(MessageTable mm) {
    myTable = mm;
    rowButton.setMargin(new Insets(0,1,0,1));
    rowButton.setFont(new Font(rowButton.getFont().getName(), Font.BOLD, rowButton.getFont().getSize()));
    rowButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try{
              myTable.showEditDialog("Edit", lastSelectedRow);
            }catch(Exception ex){
              ex.printStackTrace();
            }
          }
        });
  }

  public  Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    // Edit button in first column
    lastSelectedRow = row;
    if(Integer.class.isInstance(value)){
      rowButton.setText(""+(row+1));
      return rowButton;
    }

    MultiSelectPropertyBox myMultiSelectPropertyBox = null;
    String myPropertyType = null;
    PropertyBox myPropertyBox = null;
    PropertyField myPropertyField = null;
    PropertyCheckBox myPropertyCheckBox = null;
    DatePropertyField myDatePropertyField = null;
    IntegerPropertyField myIntegerPropertyField = null;
    FloatPropertyField myFloatPropertyField = null;
    MoneyField myMoneyPropertyField = null;
    PercentageField myPercentagePropertyField = null;
    ClockTimeField myClockTimeField = null;
    StopwatchTimeField myStopwatchTimeField = null;
    Border b = new LineBorder(Color.black, 2);
    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>> Editor: " + isSelected + ", " + row + ", " + column);
//    myTable = (MessageTable) table;
    lastRow = row;
    lastColumn = column;
    wasSelected = isSelected;
    myTable.setEditingColumn(column);
    myTable.setEditingRow(row);
    if (Property.class.isInstance(value)) {
      myProperty = (Property) value;
      copy = (Property) myProperty.clone();
      myPropertyType = (String) myProperty.getType();
      if (myPropertyType.equals(Property.SELECTION_PROPERTY)) {

        try {
          if (row == 0) {
//            System.err.println("\n\n=====================================================================================");
            for (int i = 0; i < myProperty.getAllSelections().size(); i++) {
//              System.err.println("EDITOR SEL: " + ( (Selection) myProperty.getAllSelections().get(i)).getName() + " selected: " + ( (Selection) myProperty.getAllSelections().get(i)).isSelected());
            }
//            System.err.println("=====================================================================================");
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        if(myProperty.getCardinality().equals("+")){
          if (myMultiSelectPropertyBox == null) {
            myMultiSelectPropertyBox = new MultiSelectPropertyBox();
            myMultiSelectPropertyBox.addItemListener(new ItemListener() {
              public void itemStateChanged(ItemEvent e) {
                System.err.println(">>> " + e.getStateChange());
                if (e.SELECTED == e.getStateChange()) {
//                ( (PropertyControlled) e.getSource()).update();
                  if (!isChangingSelection) {
                    System.err.println("COMBOBOX FIRED TOWARDS EDITOR");
                    stopCellEditing();
                  }

                }
              }
            });
          }
          isChangingSelection = true;
          
          myMultiSelectPropertyBox.setProperty(myProperty);
          myMultiSelectPropertyBox.setEditable(myProperty.isDirIn());
          lastComponent = myMultiSelectPropertyBox;
          setComponentColor(myMultiSelectPropertyBox, isSelected, row, column);
          myMultiSelectPropertyBox.setBorder(b);
          myMultiSelectPropertyBox.requestFocus();
          isChangingSelection = false;
          return myMultiSelectPropertyBox;

        }else{


          if (myPropertyBox == null) {
            myPropertyBox = new PropertyBox();
            myPropertyBox.addItemListener(new ItemListener() {
              public void itemStateChanged(ItemEvent e) {
                System.err.println(">>> " + e.getStateChange());
                if (e.SELECTED == e.getStateChange()) {
//                ( (PropertyControlled) e.getSource()).update();
                  if (!isChangingSelection) {
                    System.err.println("COMBOBOX FIRED TOWARDS EDITOR");
                    stopCellEditing();
                  }

                }
              }
            });
          }
          isChangingSelection = true;
          myPropertyBox.setProperty(myProperty);
          myPropertyBox.setEditable(myProperty.isDirIn());
          lastComponent = myPropertyBox;
          setComponentColor(myPropertyBox, isSelected, row, column);
          myPropertyBox.setBorder(b);
          myPropertyBox.requestFocus();
          isChangingSelection = false;
          return myPropertyBox;
        }
        /** @todo etc */
      }
      if (myPropertyType.equals(Property.BOOLEAN_PROPERTY)) {
        if (myPropertyCheckBox == null) {
          myPropertyCheckBox = new PropertyCheckBox();
          myPropertyCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              stopCellEditing();
              System.err.println("CHECKBOX FIRED TOWARDS EDITOR");
            }
          });
        }
//        myPropertyCheckBox.addFocusListener(new FocusAdapter(){
//            public void focusLost(FocusEvent e){
//              ((PropertyControlled)e.getSource()).update();
//              stopCellEditing();
//              System.err.println("CHECKOBOX FIRED TOWARDS EDITOR");
//            }
//           });

        myPropertyCheckBox.setProperty(myProperty);
        myPropertyCheckBox.setEnabled(myProperty.isDirIn());
        lastComponent = myPropertyCheckBox;
        setComponentColor(myPropertyCheckBox, isSelected, row, column);
        System.err.println("RETURNING BOOLEAN EDITOR");
        myPropertyCheckBox.setBorder(b);
        myPropertyCheckBox.requestFocus();
        return myPropertyCheckBox;
      }

      if (myPropertyType.equals(Property.DATE_PROPERTY)) {
        if (myDatePropertyField == null) {
          myDatePropertyField = new DatePropertyField();
        }
        myDatePropertyField.setEditable(myProperty.isDirIn());
        myDatePropertyField.setProperty(myProperty);

        myDatePropertyField.setTable(myTable);
        myDatePropertyField.setSelectedCell(row, column);

        lastComponent = myDatePropertyField;
        setComponentColor(myDatePropertyField, isSelected, row, column);
        myDatePropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
             ( (PropertyControlled) e.getSource()).update(); stopCellEditing();
          }
        });
        myDatePropertyField.setBorder(b);
        myDatePropertyField.requestFocus();
        myDatePropertyField.selectAll();
        return myDatePropertyField;
      }

      if (myPropertyType.equals(Property.INTEGER_PROPERTY)) {
        if (myIntegerPropertyField == null) {
          myIntegerPropertyField = new IntegerPropertyField();
        }
        myIntegerPropertyField.setEditable(myProperty.isDirIn());
        myIntegerPropertyField.setProperty(myProperty);
        lastComponent = myIntegerPropertyField;
        setComponentColor(myIntegerPropertyField, isSelected, row, column);
        myIntegerPropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
//                ((PropertyControlled)e.getSource()).update();
            stopCellEditing();
          }
        });
//        myIntegerPropertyField.addKeyListener(new KeyAdapter() {
//          public void keyPressed(KeyEvent e) {
//            System.err.println("Pressed a key in integerpropfield");
//            ( (PropertyControlled) lastComponent).update();
//          }
//        });
//        System.err.println("returning integer propertyfield");
        myIntegerPropertyField.selectAll();
        myIntegerPropertyField.setBorder(b);
        myIntegerPropertyField.setRequestFocusEnabled(true);
        myIntegerPropertyField.requestFocus();
        try {
//          myIntegerPropertyField.update();
          myIntegerPropertyField.getProperty().setValue(myIntegerPropertyField.getText());
        }
        catch (PropertyTypeException ex) {
          System.err.println(ex.getMessage());
          ex.printStackTrace();
          myIntegerPropertyField.setText(""+myIntegerPropertyField.getProperty().getTypedValue());
        }
         return myIntegerPropertyField;
      }

      if (myPropertyType.equals(Property.FLOAT_PROPERTY)) {
        if (myFloatPropertyField == null) {
          myFloatPropertyField = new FloatPropertyField();
        }
        myFloatPropertyField.setEditable(myProperty.isDirIn());
        lastComponent = myFloatPropertyField;
        myFloatPropertyField.setProperty(myProperty);
        setComponentColor(myFloatPropertyField, isSelected, row, column);
        myFloatPropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
//               System.err.println("Floatfield focus lost!");
//               System.err.println(">>>>"+((FloatPropertyField)e.getSource()).getText());
//               ((FloatPropertyField)e.getSource()).update();
            stopCellEditing();
          }
        });
        myFloatPropertyField.selectAll();
        myFloatPropertyField.setBorder(b);
        myFloatPropertyField.requestFocus();
        return myFloatPropertyField;
      }

      if (myPropertyType.equals(Property.MONEY_PROPERTY)) {
        System.err.println("Editing money!");
        if (myMoneyPropertyField == null) {
          myMoneyPropertyField = new MoneyField();
        }
        myMoneyPropertyField.setEditable(myProperty.isDirIn());
        lastComponent = myMoneyPropertyField;
        myMoneyPropertyField.setProperty(myProperty);
        setComponentColor(myMoneyPropertyField, isSelected, row, column);
//        final String contents = myMoneyPropertyField.getText();
        myMoneyPropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
            stopCellEditing();
          }
        });
        myMoneyPropertyField.editProperty();
        myMoneyPropertyField.selectAll();
        myMoneyPropertyField.setBorder(b);
        myMoneyPropertyField.requestFocus();
        return myMoneyPropertyField;
      }

      if (myPropertyType.equals(Property.PERCENTAGE_PROPERTY)) {
        System.err.println("Editing percentage!");
        if (myPercentagePropertyField == null) {
          myPercentagePropertyField = new PercentageField();
        }
        myPercentagePropertyField.setEditable(myProperty.isDirIn());
        lastComponent = myPercentagePropertyField;
        myPercentagePropertyField.setProperty(myProperty);
        setComponentColor(myPercentagePropertyField, isSelected, row, column);
//         final String contents = myPercentagePropertyField.getText();
        myPercentagePropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
            stopCellEditing();
          }
        });
//        myPercentagePropertyField.set(null);
//        myPercentagePropertyField.selectAll();
        myPercentagePropertyField.editProperty();
        myPercentagePropertyField.setBorder(b);
        myPercentagePropertyField.requestFocus();
        return myPercentagePropertyField;
      }

      if (myPropertyType.equals(Property.STOPWATCHTIME_PROPERTY)) {
        System.err.println("Editing a stopwatchtime!");
        if (myStopwatchTimeField == null) {
          myStopwatchTimeField = new StopwatchTimeField();
        }
        myStopwatchTimeField.setEditable(myProperty.isDirIn());

        lastComponent = myStopwatchTimeField;
        myStopwatchTimeField.setProperty(myProperty);
        setComponentColor(myStopwatchTimeField, isSelected, row, column);
        myStopwatchTimeField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
//               System.err.println("Floatfield focus lost!");
//               System.err.println(">>>>"+((FloatPropertyField)e.getSource()).getText());
//               ((FloatPropertyField)e.getSource()).update();
            stopCellEditing();
          }
        });
        myStopwatchTimeField.focusGained(null);
        myStopwatchTimeField.selectAll();
        myStopwatchTimeField.setBorder(b);
        myStopwatchTimeField.requestFocus();


//        Dit werkt niet... raar is dat

//        myClockTimeField.addKeyListener(new KeyAdapter() {
//          public void keyPressed(KeyEvent e) {
//            System.err.println("Key: " + e.getKeyText(e.getKeyCode()));
//            if ("Left".equals(e.getKeyText(e.getKeyCode())) || "Right".equals(e.getKeyText(e.getKeyCode())) || "Tab".equals(e.getKeyText(e.getKeyCode()))) {
//              if (myTable.isCellEditable(myTable.getSelectedRow(), myTable.getSelectedColumn())) {
//                myTable.editCellAt(myTable.getSelectedRow(), myTable.getSelectedColumn());
//              }
//            }
//          }
//        });

        return myStopwatchTimeField;
      }

      if (myPropertyType.equals(Property.CLOCKTIME_PROPERTY)) {
        System.err.println("Editing a time!");
        if (myClockTimeField == null) {
          myClockTimeField = new ClockTimeField();
        }
        myClockTimeField.setEditable(myProperty.isDirIn());
        myClockTimeField.showSeconds(false);
        lastComponent = myClockTimeField;
        myClockTimeField.setProperty(myProperty);
        setComponentColor(myClockTimeField, isSelected, row, column);
        myClockTimeField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
//               System.err.println("Floatfield focus lost!");
//               System.err.println(">>>>"+((FloatPropertyField)e.getSource()).getText());
//               ((FloatPropertyField)e.getSource()).update();
            stopCellEditing();
          }
        });
        myClockTimeField.focusGained(null);
        myClockTimeField.selectAll();
        myClockTimeField.setBorder(b);
        myClockTimeField.requestFocus();


//        Dit werkt niet... raar is dat

//        myClockTimeField.addKeyListener(new KeyAdapter() {
//          public void keyPressed(KeyEvent e) {
//            System.err.println("Key: " + e.getKeyText(e.getKeyCode()));
//            if ("Left".equals(e.getKeyText(e.getKeyCode())) || "Right".equals(e.getKeyText(e.getKeyCode())) || "Tab".equals(e.getKeyText(e.getKeyCode()))) {
//              if (myTable.isCellEditable(myTable.getSelectedRow(), myTable.getSelectedColumn())) {
//                myTable.editCellAt(myTable.getSelectedRow(), myTable.getSelectedColumn());
//              }
//            }
//          }
//        });

        return myClockTimeField;
      }

      if (myPropertyField == null) {
        myPropertyField = new TextPropertyField();
        myPropertyField.addFocusListener(new FocusAdapter() {
          public void focusLost(FocusEvent e) {
//              ((PropertyControlled)e.getSource()).update();
            stopCellEditing();
            System.err.println("PROPERTYFIELD FIRED TOWARDS EDITOR");
          }
        });
      }
      myPropertyField.setEditable(myProperty.isDirIn());
      myPropertyField.setProperty(myProperty);
      lastComponent = myPropertyField;
      setComponentColor(myPropertyField, isSelected, row, column);
      myPropertyField.setBorder(b);
      myPropertyField.requestFocus();
      myPropertyField.selectAll();
      return myPropertyField;

    }
    if (myPropertyField == null) {
      myPropertyField = new TextPropertyField();
    }
//      System.err.println("CLASS: "+value.getClass());
//    System.err.println("Oh dear, strange property type...");
    myPropertyField.setEditable(false);
//    myPropertyField.setName("unloaded_property");
    setComponentColor(myPropertyField, isSelected, row, column);
    myPropertyField.setText("..");
    myPropertyField.setBorder(b);
    myPropertyField.requestFocus();
    myPropertyField.selectAll();
    return myPropertyField;
  }

  private final void setComponentColor(Component c, boolean isSelected, int row, int column) {
    if (c == null) {
      return;
    }
    JComponent cc = (JComponent) c;
    if (isSelected) {
      cc.setBorder(new LineBorder(Color.black, 2));
    }
    else {
      cc.setBorder(null);
    }
//    if (isSelected) {
    c.setBackground(new Color(200, 200, 235));
//    } else {
//      if (row%2==0) {
//        c.setBackground(Color.white);
//      } else {
//        c.setBackground(new Color(240,240,240));
//      }
//    }
  }

//  private fireUpdate(

  public  void cancelCellEditing() {
    for (int i = 0; i < myListeners.size(); i++) {
      CellEditorListener ce = (CellEditorListener) myListeners.get(i);
      ce.editingCanceled(new ChangeEvent(myTable));
    }
  }

  public  boolean stopCellEditing() {
//    System.err.println("--------------------------------------------------------------->> Entering stopCellEditor!!!");
    if (lastComponent != null) {
      try {
        ( (PropertyControlled) lastComponent).update();
      }
      catch (PropertyTypeException ex1) {
        System.err.println(ex1.getMessage());
      }
      FocusListener[] fl = lastComponent.getFocusListeners();
      for (int i = 0; i < fl.length; i++) {
        lastComponent.removeFocusListener(fl[i]);
      }

      myTable.removeEditor();

      /** @todo Occasional null pointer here. Fix */

      if (lastComponent != null && ((PropertyControlled) lastComponent).getProperty() != null && ((PropertyControlled) lastComponent).getProperty().getType().equals(Property.SELECTION_PROPERTY)) {
       ( (PropertyControlled) lastComponent).setProperty(null);
    }
    }

    if(lastRow > -1){
      for (int i = 0; i < myListeners.size(); i++) {
        CellEditorListener ce = (CellEditorListener) myListeners.get(i);
        ce.editingStopped(new MessageTableChangeEvent(myTable, lastRow, lastColumn));
      }
    }
    return true;
  }

  public  Object getCellEditorValue() {
    return myProperty;
  }


  public  boolean isCellEditable(EventObject parm1) {

    if (myTable!=null) {
      boolean b = myTable.getModel().isCellEditable(myTable.getSelectedRow(),myTable.getSelectedColumn());
//      System.err.println("Returning: " +b);
      return b;
    }
    System.err.println("Returning false");
    return false;
  }

  public  Property getInitialProperty() {
    return copy;
  }

  public  Property getProperty() {
    return myProperty;
  }

  public  void valueChanged(ListSelectionEvent e) {
    /**@todo: Implement this javax.swing.event.ListSelectionListener method*/
    throw new java.lang.UnsupportedOperationException("Method valueChanged() not yet implemented.");
  }

  public  boolean shouldSelectCell(EventObject parm1) {
    return true;
  }

  public  void addCellEditorListener(CellEditorListener ce) {
    myListeners.add(ce);
  }

  public  void removeCellEditorListener(CellEditorListener ce) {
    myListeners.remove(ce);
  }
}
