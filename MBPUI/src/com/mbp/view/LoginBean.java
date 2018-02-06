package com.mbp.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.input.RichInputNumberSpinbox;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import com.mbp.model.module.BMPLoginAMImpl;
import com.mbp.view.utils.utils.ADFUtils;
import com.mbp.view.utils.utils.JSFUtils;

import java.text.DecimalFormat;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.output.RichOutputText;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;


/**
 *  Login page managebean
 */
public class LoginBean{
    private RichInputText username;
    private RichInputText passward;
    private RichOutputText showUsername;
    private RichSelectOneChoice showTime;
    private RichOutputText movieName;
    private RichOutputText price;
    private RichOutputText movieId;
    private RichInputNumberSpinbox ticketNumber;
    private RichOutputText discountPrice;

    public LoginBean() {
        super();
    }

    public void setUsername(RichInputText username) {
        this.username = username;
    }

    public RichInputText getUsername() {
        return username;
    }

    public void setPassward(RichInputText passward) {
        this.passward = passward;
    }

    public RichInputText getPassward() {
        return passward;
    }

    public void setShowUsername(RichOutputText showUsername) {
        this.showUsername = showUsername;
    }

    public RichOutputText getShowUsername() {
        return showUsername;
    }

    public void setShowTime(RichSelectOneChoice showTime) {
        this.showTime = showTime;
    }

    public RichSelectOneChoice getShowTime() {
        return showTime;
    }

    public void setMovieName(RichOutputText movieName) {
        this.movieName = movieName;
    }

    public RichOutputText getMovieName() {
        return movieName;
    }

    public void setPrice(RichOutputText price) {
        this.price = price;
    }

    public RichOutputText getPrice() {
        return price;
    }

    public void setMovieId(RichOutputText movieId) {
        this.movieId = movieId;
    }

    public RichOutputText getMovieId() {
        return movieId;
    }

    public void setTicketNumber(RichInputNumberSpinbox ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public RichInputNumberSpinbox getTicketNumber() {
        return ticketNumber;
    }


    public void setDiscountPrice(RichOutputText discountPrice) {
        this.discountPrice = discountPrice;
    }

    public RichOutputText getDiscountPrice() {
        return discountPrice;
    }


    /*
     * Login method
     * @return
     * */
    public String loginAction() {
        // Get username and password
        String name = (String)username.getValue();
        String pass = (String)passward.getValue();

        BMPLoginAMImpl am =
            (BMPLoginAMImpl)ADFUtils.getApplicationModuleForDataControl("BMPLoginAMDataControl");
        boolean flag = false;

        flag = am.findUserByAttr(name, pass);

        // direct call the operation in AM
        /*
        BindingContext bctx = BindingContext.getCurrent();
        BindingContainer bindings = bctx.getCurrentBindingsEntry();
        OperationBinding operationBinding = bindings.getOperationBinding("findUserByAttr");
        //optional
        operationBinding.getParamsMap().put("username",name);
        operationBinding.getParamsMap().put("passward",pass);
        //invoke method
        operationBinding.execute();
        flag = Boolean.parseBoolean(operationBinding.getResult().toString());
        System.out.println("Result from findUserByAttr method: " + flag);

        if (!operationBinding.getErrors().isEmpty()) {
            //check errors
            List errors = operationBinding.getErrors();
        }
        */
        if (flag) {
            return "success";
        }
        return "error";
    }

    /*
     * create movie ticket method
     * @return action Reuslt for page render
     * */
    public String createBookingTicket() {
        // Get parameters
        String name = (String)username.getValue();
        String pass = (String)passward.getValue();
        String showtime = (String)showTime.getValue();
        int tickets = Integer.parseInt(ticketNumber.getValue().toString());
        String moviename = movieName.getValue().toString();
        double movieprice = Double.parseDouble(discountPrice.getValue().toString());

        System.out.println("Ticket info in loginBean --" +
                           "\n    show time:      " + showtime +
                           "\n    user name:      " + name +
                           "\n    tickets:        " + tickets +
                           "\n    movie name:     " + moviename +
                           "\n    price:          " + movieprice);

        // find the AM
        BMPLoginAMImpl am =
            (BMPLoginAMImpl)ADFUtils.getApplicationModuleForDataControl("BMPLoginAMDataControl");
        String actionResult =
            am.createBooking(name, pass, showtime, moviename, movieprice, tickets);

        return actionResult;
    }


    // method before movie list page load to set login username
    public void beforePageLoad(PhaseEvent phaseEvent) {
        this.showUsername.setValue("Welcome, " + username.getValue());
    }

    public void beforeMovieDetailPageLoad(PhaseEvent phaseEvent) {
        // Get parameters
        String name = (String)username.getValue();
        String pass = (String)passward.getValue();
        int movieprice = Integer.parseInt(price.getValue().toString());
        
        // find the AM
        BMPLoginAMImpl am =
            (BMPLoginAMImpl)ADFUtils.getApplicationModuleForDataControl("BMPLoginAMDataControl");
        double disPrice = am.calDiscountPrice(name, pass, movieprice);
        
        // round the disPrice
        DecimalFormat df = new DecimalFormat("#.00");
        this.discountPrice.setValue(df.format(disPrice));
    }


    public void ticketNumberValidator(FacesContext facesContext,
                                      UIComponent uIComponent, Object object) {
        // find the AM
        BMPLoginAMImpl am =
            (BMPLoginAMImpl)ADFUtils.getApplicationModuleForDataControl("BMPLoginAMDataControl");
        int avaliableSeat = am.getAvaliableSeat(showTime.getValue().toString(), movieName.getValue().toString());
        int tickets = Integer.parseInt(object.toString());
        
        // via input tickets to judge the validator exception
        if(tickets > avaliableSeat) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "Tickets must small than avaliable!", null)); 
        }else if (tickets <= 0) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "Tickets must large than 0!", null));
        }
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public String ctb2_action() {
        // do cancle booking action
        BindingContainer bindings = getBindings();
        
        // --- GET deleted row's parameter
        // Get bindings associated with the current scope, then access the one that we have assigned to our table - e.g. OpenSupportItemsIterator
        DCIteratorBinding dcItteratorBindings = (DCIteratorBinding)bindings.get("Ticket1Iterator");
         
        // Get an object representing the table and what may be selected within it
        ViewObject voTableData = dcItteratorBindings.getViewObject();
         
        // Get selected row
        Row rowSelected = voTableData.getCurrentRow();
        
        // release the booked tickets
        int sn = Integer.parseInt(rowSelected.getAttribute("ScreenNumber").toString());
        int tn = Integer.parseInt(rowSelected.getAttribute("SeatNedded").toString());
        String st = rowSelected.getAttribute("Showtime").toString();
        
        System.out.println("\nCancle ticket ID ----- " + rowSelected.getAttribute("TicketId"));
        // do cancle
        OperationBinding operaBindingDelete = bindings.getOperationBinding("Delete");
        Object deleteResult = operaBindingDelete.execute();
        System.out.println("Delete action done!");
        
        // find the AM do release tickets
        BMPLoginAMImpl am =
            (BMPLoginAMImpl)ADFUtils.getApplicationModuleForDataControl("BMPLoginAMDataControl");
        am.updateAvaliableSeat(tn, sn, st); // tickets number, screen number, show time
        
        
        // do commit cancle booking action
        OperationBinding operaBindingCommit = bindings.getOperationBinding("Commit");
        Object commitResult = operaBindingCommit.execute();
        System.out.println("Delete commit action done!");
        
        if (!operaBindingDelete.getErrors().isEmpty() || !operaBindingCommit.getErrors().isEmpty()) {
            return null;
        }
        return null;
    }

    public void selectShowTimeValueChange(ValueChangeEvent valueChangeEvent) {
        UIComponent component = valueChangeEvent.getComponent();
        component.processUpdates(FacesContext.getCurrentInstance());
        
        String st = (String) JSFUtils.resolveExpression("#{bindings.MovieShowtime2.inputValue}");
        System.out.println("Selected showTime is ---- " + st);
    }
}
