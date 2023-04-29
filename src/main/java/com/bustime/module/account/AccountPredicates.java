package com.bustime.module.account;

import com.bustime.module.Tag.Tag;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class AccountPredicates {
    public static Predicate findByTags(Set<Tag> tags) {
        QAccount account = QAccount.account;
        return account.tags.any().in(tags);
    }

}
