package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService; // Connected File B here

   public KafkaConsumer(UserRepository userRepository, TransactionRepository transactionRepository, IncentiveService incentiveService) {
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.incentiveService = incentiveService;
}
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void listen(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord receiver = userRepository.findById(transaction.getRecipientId());

        if (sender != null && receiver != null && sender.getBalance() >= transaction.getAmount()) {
            // 1. Get the bonus amount from the external API
            Incentive incentive = incentiveService.getIncentive(transaction);
            float bonus = (incentive != null) ? incentive.getAmount() : 0f;

            // 2. Update balances (Sender loses amount, Receiver gets amount + bonus)
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            receiver.setBalance(receiver.getBalance() + transaction.getAmount() + bonus);

            // 3. Save to SQL Database
            userRepository.save(sender);
            userRepository.save(receiver);
            transactionRepository.save(new TransactionRecord(sender, receiver, transaction.getAmount()));

System.out.println(">>> FINAL CHECK: " + receiver.getName() + " now has " + receiver.getBalance());        }
    }
}