package net.purefunc.practice.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet")
@Entity
public class WalletPO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
}
