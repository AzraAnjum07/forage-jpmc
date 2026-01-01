package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Component;

@Component
public class IncentiveService {
    
    public IncentiveService() {
       
    }

    public Incentive getIncentive(Transaction transaction) {
        
        return new Incentive(0f); 
    }
}