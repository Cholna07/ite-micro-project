package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.persistence.entity.UserJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.UserPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserRepositoryAdapterPaginationTest {
    @Test
    void databaseSpecificationRetainsKeywordAndStatusFilters() {
        var adapter = new UserRepositoryAdapter(mock(UserJpaRepository.class), mock(UserPersistenceMapper.class));
        UserFilterQuery query = new UserFilterQuery("alice", true, false, false, 0, 10, "username", "ASC");
        Root<UserJpaEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);

        adapter.specification(query).toPredicate(root, criteriaQuery, builder);

        verify(root).get("username");
        verify(root).get("email");
        verify(root).get("enabled");
        verify(root).get("emailVerified");
        verify(root).get("isDeleted");
        verify(builder, times(2)).like(any(), eq("%alice%"), eq('\\'));
        verify(builder).equal(any(), eq(true));
        verify(builder, times(2)).equal(any(), eq(false));
    }

    @Test
    void authenticatableUserFilterRequiresEnabledUnlockedAndNotDeleted() {
        var adapter = new UserRepositoryAdapter(mock(UserJpaRepository.class), mock(UserPersistenceMapper.class));
        UserFilterQuery query = new UserFilterQuery(null, true, null, false, false, 0, 1, "username", "ASC");
        Root<UserJpaEntity> root = mock(Root.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        adapter.specification(query).toPredicate(root, mock(CriteriaQuery.class), builder);

        verify(root).get("enabled");
        verify(root).get("accountNonLocked");
        verify(root).get("isDeleted");
        verify(builder, times(2)).equal(any(), eq(true));
        verify(builder).equal(any(), eq(false));
    }

    @Test
    void delegatesFilteringAndDatabasePaginationToJpaAndReturnsPageTotals() {
        UserJpaRepository jpa = mock(UserJpaRepository.class);
        UserPersistenceMapper mapper = mock(UserPersistenceMapper.class);
        UserJpaEntity entity = new UserJpaEntity();
        User domain = mock(User.class);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(jpa.findAll(ArgumentMatchers.<Specification<UserJpaEntity>>any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 5));
        var adapter = new UserRepositoryAdapter(jpa, mapper);
        var query = new UserFilterQuery("alice", true, false, false, 1, 2, "createdAt", "DESC");

        PageResult<User> result = adapter.findAll(query);

        assertEquals(List.of(domain), result.content());
        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());
        assertFalse(result.first());
        assertFalse(result.last());
        var pageable = org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(jpa).findAll(ArgumentMatchers.<Specification<UserJpaEntity>>any(), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(2, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("createdAt").getDirection());
        verify(jpa, never()).findAll();
    }
}
