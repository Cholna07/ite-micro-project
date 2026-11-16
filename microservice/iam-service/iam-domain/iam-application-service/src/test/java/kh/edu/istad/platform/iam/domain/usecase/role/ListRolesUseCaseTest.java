package kh.edu.istad.platform.iam.domain.usecase.role;

import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.dto.result.PageResult;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.valueobject.RoleName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListRolesUseCaseTest {
    @Test
    void returnsDatabasePageMetadataAndForwardsRequestedFilterSort() {
        Role role = new Role(); role.setId(1L); role.setUuid(UUID.randomUUID()); role.setName(new RoleName("AUDITOR")); role.setActive(true);
        RoleRepository repository = mock(RoleRepository.class);
        RoleFilterQuery query = new RoleFilterQuery("audit", true, false, false, 2, 5, "name", "DESC");
        when(repository.findPage(query)).thenReturn(new PageResult<>(List.of(role), 2, 5, 11, 3, false, true));

        var result = new ListRolesUseCase(repository).execute(query);

        assertEquals(1, result.content().size()); assertEquals("AUDITOR", result.content().getFirst().name());
        assertEquals(11, result.totalElements()); assertEquals(3, result.totalPages()); assertFalse(result.first()); assertTrue(result.last());
        verify(repository).findPage(query); verify(repository, never()).findAll();
    }

    @Test
    void rejectsInvalidPageSizeSortFieldAndDirection() {
        RoleRepository repository = mock(RoleRepository.class); var useCase = new ListRolesUseCase(repository);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(-1, 10, "name", "ASC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 101, "name", "ASC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 10, "uuid", "ASC")));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(query(0, 10, "name", "sideways")));
        verifyNoInteractions(repository);
    }

    private RoleFilterQuery query(int page, int size, String sortBy, String direction) { return new RoleFilterQuery(null, null, null, null, page, size, sortBy, direction); }
}
