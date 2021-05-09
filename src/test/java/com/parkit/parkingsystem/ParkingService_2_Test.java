package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingService_2_Test {

	private static ParkingService parkingService;
	
    @Mock
    private static TicketDAO ticketDAO;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    private void setUpPerTest() {
    	//
    	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processIncomingVehicle_CAR_UpdateParking_AND_SaveTicket_Test(){
        try {
        	
        	//------------- ARRANGE 
        	
        	// Le type du véhicule => ParkingType.CAR
        	when(inputReaderUtil.readSelection()).thenReturn(1);
        	
        	// Prochaine place de parking libre
        	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        	
        	// Immatriculation du véhicule
        	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        	
        	// Mise à jour du parking avec la nouvelle entrée
        	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        	
        	// Sauvegarde du ticket généré
        	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        	// Vérification si utilisateur récurent
        	when(ticketDAO.verifyRecuring("ABCDEF")).thenReturn(false);
        	
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }

    	//------------- ACT 
        parkingService.processIncomingVehicle();
        
    	//------------- ASSERT 
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).verifyRecuring("ABCDEF");
    }
    
    @Test
    public void processIncomingVehicle_NoAvailabaleParkingSlot_NoUpdate_Test(){
    	// Le type du véhicule => ParkingType.CAR
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	// Prochaine place de parking non libre
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
    	//
        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.never()).saveTicket(any(Ticket.class));
    }

    @Test
    public void processIncomingVehicleTest_WrongType_NoUpdate_Test(){
    	// Le type du véhicule => ParkingType.CAR
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	//
        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.never()).saveTicket(any(Ticket.class));
    }  
}
