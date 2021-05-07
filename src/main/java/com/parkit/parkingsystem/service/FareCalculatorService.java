package com.parkit.parkingsystem.service;

import java.sql.SQLException;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
    private static TicketDAO ticketDAO = new TicketDAO();

    public static void setTicketDAO(TicketDAO ticketDAO) {
		FareCalculatorService.ticketDAO = ticketDAO;
	}

	public void calculateFare(Ticket ticket) throws ClassNotFoundException, SQLException{

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time and In time provided are incorrect: // InTime : "+ticket.getInTime().toString()+" // OutTime : "+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = ((float)(outHour - inHour))/3600000.0;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration  * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration  * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        } 
        if (duration <= 0.5)
        	ticket.setPrice(0);

        String vehicleRegNumber = ticket.getVehicleRegNumber();
        
        if(ticketDAO.verifyRecuring(vehicleRegNumber))
        	ticket.setPrice(ticket.getPrice()*0.95);
        ticket.setPrice((Math.round(ticket.getPrice() * 100)) / 100.0);
    }
}