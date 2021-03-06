/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mechachatapp.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Stack;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import mechachatapp.be.Message;
import mechachatapp.bll.exceptions.BllException;
import mechachatapp.gui.model.MechaChatLogModel;
import mechachatapp.gui.model.command.CreateMessageCommand;
import mechachatapp.gui.model.command.ICommand;

/**
 *
 * @author pgn
 */
public class MessageLogViewController implements Initializable
{

    @FXML
    private ListView<Message> lstMessages;
    @FXML
    private TextField txtMessage;

    private MechaChatLogModel model;

    private Stack<ICommand> undos;
    private Stack<ICommand> redos;

    private KeyCombination keysUndo;
    private KeyCombination keysRedo;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        keysUndo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        keysRedo = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);

        try
        {
            model = new MechaChatLogModel();
            undos = new Stack<>();
            redos = new Stack<>();
            lstMessages.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>()
            {
                @Override
                public ListCell<Message> call(ListView<Message> param)
                {
                    ListCell<Message> cell = new ListCell<Message>()
                    {
                        @Override
                        protected void updateItem(Message item, boolean empty)
                        {
                            super.updateItem(item, empty); 
                            if (!empty && item != null)
                            {
                                setText("#" + item.getId() + ": " + item.getText());
                            }
                        }

                    };
                    return cell;
                }
            });
            lstMessages.setItems(model.getMessages());
            initKeyHandling();
        } catch (BllException ex)
        {
            displayException(ex);
        }
    }

    private void initKeyHandling()
    {
        txtMessage.getParent().addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent event)
            {
                if (keysUndo.match(event))
                {
                    undo();
                } else if (keysRedo.match(event))
                {
                    redo();
                }
            }
        });
    }

    private void displayException(BllException ex)
    {
        System.out.println("I should display an error message wit the text: " + ex.getMessage());
        ex.printStackTrace();
    }

    @FXML
    private void handleSendMessage(ActionEvent event)
    {
        String txt = txtMessage.getText();
        CreateMessageCommand cmdCreate = new CreateMessageCommand(model, txt);
        issueCommand(cmdCreate);
    }

    private void issueCommand(ICommand command)
    {
        try
        {
            command.execute();
            undos.push(command);
            redos.clear();
        } catch (BllException ex)
        {
            displayException(ex);
        }
    }

    private void undo()
    {
        try
        {
            if (!undos.empty())
            {
                ICommand cmd = undos.pop();
                cmd.undo();
                redos.push(cmd);
            }
        } catch (BllException ex)
        {
            displayException(ex);
        }
    }

    private void redo()
    {
        try
        {
            if (!redos.empty())
            {
                ICommand cmd = redos.pop();
                cmd.execute();
                undos.push(cmd);
            }
        } catch (BllException ex)
        {
            displayException(ex);
        }
    }

}
