package com.kmbbj.backend.balance.entity;

import com.kmbbj.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

//자산
@ToString
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "total_balances")
public class TotalBalance {
    //토탈 계좌 식별값
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "total_balance_id")
    private Long totalBalanceId;

    //현재 보유 자산
    @Column(name = "assets", nullable = false)
    private Long asset;

    //계좌 소유 user
    @JoinColumn(name = "user_id", nullable = false)
    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    /**
     * 보유 자산 변경 메서드
     * @param changeAssets 변경될 계좌 값 +- 변경되는 값 적어주면 된다.
     * @return 변경된 TotalBalance
     */
    public TotalBalance changeAsset(Long changeAssets) {
        this.asset = this.asset + changeAssets;
        return this;
    }
}
