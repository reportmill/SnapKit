/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.geom.Insets;
import snap.gfx.*;
import snap.util.Convert;
import snap.view.*;

/**
 * A class to build a form.
 */
public class FormBuilder extends ViewOwner {

    // The root pane
    protected ColView  _formView;

    // The font
    private Font  _font;
    
    // Form values
    private Map<String,Object>  _values = new HashMap<>();

    /**
     * Constructor.
     */
    public FormBuilder()
    {
        super();
        _formView = createFormView();
    }

    /**
     * Returns the padding.
     */
    public Insets getPadding()  { return _formView.getPadding(); }

    /**
     * Sets the padding.
     */
    public void setPadding(Insets theInsets)  { _formView.setPadding(theInsets); }

    /**
     * Sets the padding.
     */
    public void setPadding(double aTp, double aRt, double aBt, double aLt)
    {
        setPadding(new Insets(aTp, aRt, aBt, aLt));
    }

    /**
     * Returns the spacing between components.
     */
    public double getSpacing()  { return _formView.getSpacing(); }

    /**
     * Sets the spacing between components.
     */
    public void setSpacing(double aValue)  { _formView.setSpacing(aValue); }

    /**
     * Returns the font.
     */
    public Font getFont()  { return _font; }

    /**
     * Sets the font.
     */
    public void setFont(Font aFont)  { _font = aFont; }

    /**
     * Adds a label.
     */
    public Label addLabel(String aTitle)
    {
        Label label = new Label();
        label.setText(aTitle);
        if (_font != null)
            label.setFont(_font);
        return addView(label);
    }

    /**
     * Adds a TextArea.
     */
    public TextArea addTextArea(String aTitle)
    {
        TextArea text = new TextArea();
        text.setText(aTitle);
        if (_font != null)
            text.setFont(_font);
        return addView(text);
    }

    /**
     * Adds a separator.
     */
    public Separator addSeparator()
    {
        Separator sep = new Separator();
        return addView(sep);
    }

    /**
     * Adds a text field.
     */
    public TextField addTextField(String aName, String aDefault)
    {
        return addTextField(null, aName, aDefault);
    }

    /**
     * Adds a text field.
     */
    public TextField addTextField(String aLabel, String aName, String aDefault)
    {
        // Create RowView for label and text field
        RowView rowView = new RowView();

        // If label is provided, create configure and add
        if (aLabel != null) {
            Label label = new Label();
            label.setText(aLabel);
            if (_font != null)
                label.setFont(_font);
            rowView.addChild(label);
        }

        // Create TextField and panel and add
        TextField textField = new TextField();
        textField.setName(aName);
        if (_font != null)
            textField.setFont(_font);
        if (aDefault != null)
            textField.setText(aDefault);
        rowView.addChild(textField);

        // Add RowView
        addView(rowView);

        // Add binding
        //addViewBinding(textField, View.Text_Prop, aName.replace(" ", ""));
        //if (aDefault != null) setValue(aName, aDefault);

        // Set FirstFocus
        if (getFirstFocus() == null)
            setFirstFocus(textField);

        // Return
        return textField;
    }

    /**
     * Adds an option field.
     */
    public ComboBox addComboBox(String aTitle, String[] options, String aDefault)
    {
        // Create ComboBox and panel and add
        Label label = new Label();
        label.setText(aTitle + ":");
        ComboBox<?> comboBox = new ComboBox<>();
        comboBox.setName(aTitle);
        RowView rowView = new RowView();
        rowView.addChild(label);
        rowView.addChild(comboBox);
        addView(rowView);

        // Add binding
        //String bindingKey = aTitle.replace(" ", "");
        //addViewBinding(comboBox, Selectable.SelItem_Prop, bindingKey);
        //setValue(aTitle, aDefault);

        // Return
        return comboBox;
    }

    /**
     * Adds buttons.
     */
    public RowView addButtons(String[] theTitles)
    {
        String[] names = new String[theTitles.length];
        for (int i = 0; i < theTitles.length; i++)
            names[i] = theTitles[i] + "Button";
        return addButtons(names, theTitles);
    }

    /**
     * Adds buttons.
     */
    public RowView addButtons(String[] theNames, String[] theLabels)
    {
        RowView rowView = new RowView();
        rowView.setSpacing(10); //panel.setAlignmentX(0);

        // Iterate over options
        for (int i = 0, iMax = theNames.length; i<iMax; i++) {
            String title = theNames[i];
            String text = theLabels[i];
            Button button = new Button();
            button.setName(title);
            button.setText(text);
            rowView.addChild(button);
        }

        // Add/return hbox
        return addView(rowView);
    }

    /**
     * Adds radio buttons.
     */
    public List <RadioButton> addRadioButtons(String aTitle, String[] options, String aDefault)
    {
        List<RadioButton> radioButtons = new ArrayList<>();
        for (String option : options)
            radioButtons.add(addRadioButton(aTitle, option, option.equals(aDefault)));
        return radioButtons;
    }

    /**
     * Adds a radio button.
     */
    public RadioButton addRadioButton(String aTitle, String theText, boolean isSelected)
    {
        // Create radio button, add to button group and add to panel
        RadioButton radioButton = new RadioButton();
        radioButton.setName(aTitle);
        radioButton.setText(theText);
        if (_font != null)
            radioButton.setFont(_font);
        if (isSelected) {
            radioButton.setSelected(true);
            setValue(aTitle, theText);
        }
        radioButton.setGroupName(aTitle);

        // Add/return button
        return addView(radioButton);
    }

    /**
     * Adds a View.
     */
    public <T extends View> T addView(T aView)
    {
        _formView.addChild(aView);
        return aView;
    }

    /**
     * Removes a View.
     */
    public <T extends View> T removeView(T aView)
    {
        _formView.removeChild(aView);
        return aView;
    }

    /**
     * Show Dialog.
     */
    public boolean showPanel(View aView, String aTitle, Image anImage)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setImage(anImage);
        dialogBox.setContent(getUI());
        return dialogBox.showConfirmDialog(aView);
    }

    /**
     * Returns the specified value.
     */
    public Object getValue(String aKey)
    {
        String key = aKey.replace(" ", "");
        return _values.get(key);
    }

    /**
     * Sets the specified value.
     */
    public void setValue(String aKey, Object aValue)
    {
        String key = aKey.replace(" ", "");
        _values.put(key, aValue);
    }

    /**
     * Returns the specified value.
     */
    public String getStringValue(String aKey)
    {
        Object keyValue = getValue(aKey);
        return Convert.stringValue(keyValue);
    }

    /**
     * Creates the FormView.
     */
    protected ColView createFormView()
    {
        ColView formView = new ColView();
        formView.setPadding(new Insets(8));
        formView.setSpacing(20);
        formView.setFillWidth(true);
        return formView;
    }

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        return _formView;
    }

    /**
     * Responds to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle RadioButtons
        if (anEvent.getView() instanceof RadioButton)
            setValue(anEvent.getName(), anEvent.getText());
    }
}