package com.piseth.java.school.addressservice.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.piseth.java.school.addressservice.domain.AdminArea;
import com.piseth.java.school.addressservice.domain.enumeration.AdminLevel;
import com.piseth.java.school.addressservice.dto.AdminAreaCreateRequest;
import com.piseth.java.school.addressservice.dto.AdminAreaResponse;
import com.piseth.java.school.addressservice.dto.AdminAreaUpdateRequest;
import com.piseth.java.school.addressservice.exception.AdminAreaNotFoundException;
import com.piseth.java.school.addressservice.exception.ChildrenExistException;
import com.piseth.java.school.addressservice.exception.DuplicateAdminAreaException;
import com.piseth.java.school.addressservice.exception.ParentNotFoundException;
import com.piseth.java.school.addressservice.mapper.AdminAreaMapper;
import com.piseth.java.school.addressservice.repository.AdminAreaRepository;
import com.piseth.java.school.addressservice.validator.AdminAreaValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AdminAreaServiceImplTest {

    @Mock
    private AdminAreaRepository repo;
    @Mock
    private AdminAreaMapper mapper;
    @Mock
    private AdminAreaValidator validator;

    @InjectMocks
    private AdminAreaServiceImpl service;

    // ---------- helpers ----------
    private static AdminArea entity(String code, AdminLevel level, String parent, String nameKh, String nameEn) {
        AdminArea a = new AdminArea();
        a.setCode(code);
        a.setLevel(level);
        a.setParentCode(parent);
        a.setNameKh(nameKh);
        a.setNameEn(nameEn);
        return a;
    }

    private static AdminAreaResponse resp(String code, String nameKh, String nameEn) {
        AdminAreaResponse r = new AdminAreaResponse();
        r.setCode(code);
        r.setNameKh(nameKh);
        r.setNameEn(nameEn);
        return r;
    }

    // ===========================================================
    // create()
    // ===========================================================
    @Nested
    class Create {

        @Test
        @DisplayName("create: PROVINCE (no parent check) -> success")
        void province_success() {
            String code = "01";
            AdminAreaCreateRequest req = mock(AdminAreaCreateRequest.class);
            AdminArea candidate = entity(code, AdminLevel.PROVINCE, null, "KH", "EN");
            AdminArea saved = entity(code, AdminLevel.PROVINCE, null, "KH", "EN");
            AdminAreaResponse out = resp(code, "KH", "EN");

            when(mapper.toEntity(req)).thenReturn(candidate);
            doNothing().when(validator).validate(candidate);

            when(repo.existsById(eq(code))).thenReturn(Mono.just(false));
            when(repo.save(candidate)).thenReturn(Mono.just(saved));
            when(mapper.toResponse(saved)).thenReturn(out);

            StepVerifier.create(service.create(req)).expectNext(out).verifyComplete();

            verify(repo, times(1)).existsById(eq(code));
            verify(repo, times(1)).existsById(anyString());
            verify(repo).save(candidate);
            verify(mapper).toEntity(req);
            verify(mapper).toResponse(saved);
            verify(validator).validate(candidate);
        }

        @Test
        @DisplayName("create: NON-PROVINCE (parent exists + no duplicate) -> success")
        void nonProvince_success() {
            String parent = "01";
            String code = "01-02"; // use "0102" if you've switched to dashless
            AdminAreaCreateRequest req = mock(AdminAreaCreateRequest.class);
            AdminArea candidate = entity(code, AdminLevel.DISTRICT, parent, "KH", "EN");
            AdminArea saved = entity(code, AdminLevel.DISTRICT, parent, "KH", "EN");
            AdminAreaResponse out = resp(code, "KH", "EN");

            when(mapper.toEntity(req)).thenReturn(candidate);
            doNothing().when(validator).validate(candidate);

            when(repo.existsById(eq(parent))).thenReturn(Mono.just(true));
            when(repo.existsById(eq(code))).thenReturn(Mono.just(false));
            when(repo.save(candidate)).thenReturn(Mono.just(saved));
            when(mapper.toResponse(saved)).thenReturn(out);

            StepVerifier.create(service.create(req)).expectNext(out).verifyComplete();

            verify(repo).existsById(parent);
            verify(repo).existsById(code);
            verify(repo).save(candidate);
            verify(mapper).toEntity(req);
            verify(mapper).toResponse(saved);
            verify(validator).validate(candidate);
        }

        @Test
        @DisplayName("create: NON-PROVINCE but parent missing -> error, save never called")
        void nonProvince_parentMissing() {
            String parent = "01";
            String code = "01-02"; // use "0102" if dashless
            AdminAreaCreateRequest req = mock(AdminAreaCreateRequest.class);
            AdminArea candidate = entity(code, AdminLevel.DISTRICT, parent, "KH", "EN");

            when(mapper.toEntity(req)).thenReturn(candidate);
            doNothing().when(validator).validate(candidate);

            when(repo.existsById(eq(parent))).thenReturn(Mono.just(false));
            when(repo.existsById(eq(code))).thenReturn(Mono.just(false));

            StepVerifier.create(service.create(req)).expectErrorSatisfies(err -> {
                Assertions.assertTrue(err instanceof ParentNotFoundException);
                Assertions.assertTrue(err.getMessage().contains("Parent not found"));
            }).verify();

            verify(repo).existsById(parent);
            verify(repo).existsById(code);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("create: duplicate code -> error, save never called")
        void duplicate_error() {
            String parent = "01";
            String code = "01-02"; // use "0102" if dashless
            AdminAreaCreateRequest req = mock(AdminAreaCreateRequest.class);
            AdminArea candidate = entity(code, AdminLevel.DISTRICT, parent, "KH", "EN");

            when(mapper.toEntity(req)).thenReturn(candidate);
            doNothing().when(validator).validate(candidate);

            when(repo.existsById(eq(parent))).thenReturn(Mono.just(true));
            when(repo.existsById(eq(code))).thenReturn(Mono.just(true)); // duplicate

            StepVerifier.create(service.create(req)).expectErrorSatisfies(err -> {
                Assertions.assertTrue(err instanceof DuplicateAdminAreaException);
                Assertions.assertTrue(err.getMessage().contains("already exists"));
            }).verify();

            verify(repo).existsById(parent);
            verify(repo).existsById(code);
            verify(repo, never()).save(any());
        }
    }

    // ===========================================================
    // get()
    // ===========================================================
    @Nested
    class Get {

        @Test
        void get_found() {
            String code = "01";
            AdminArea e = entity(code, AdminLevel.PROVINCE, null, "KH", "EN");
            AdminAreaResponse r = resp(code, "KH", "EN");

            when(repo.findById(code)).thenReturn(Mono.just(e));
            when(mapper.toResponse(e)).thenReturn(r);

            StepVerifier.create(service.get(code)).expectNext(r).verifyComplete();

            verify(repo).findById(code);
            verify(mapper).toResponse(e);
        }

        @Test
        void get_notFound() {
            String code = "404";
            when(repo.findById(code)).thenReturn(Mono.empty());

            StepVerifier.create(service.get(code)).expectErrorSatisfies(err -> {
                Assertions.assertTrue(err instanceof AdminAreaNotFoundException);
                Assertions.assertTrue(err.getMessage().contains("not found"));
            }).verify();

            verify(repo).findById(code);
            verify(mapper, never()).toResponse(any());
        }
    }

    // ===========================================================
    // list()
    // ===========================================================
    @Nested
    class ListBranching {

        private final Sort SORT = Sort.by(Sort.Direction.ASC, "code");

        @Test
        @DisplayName("list: no filters -> findAll(sorted)")
        void list_all_sorted() {
            AdminArea a = entity("01-01", AdminLevel.DISTRICT, "01", "A", "A");
            AdminArea b = entity("01-02", AdminLevel.DISTRICT, "01", "B", "B");
            AdminAreaResponse ra = resp("01-01", "A", "A");
            AdminAreaResponse rb = resp("01-02", "B", "B");

            when(repo.findAll(eq(SORT))).thenReturn(Flux.just(a, b));
            when(mapper.toResponse(a)).thenReturn(ra);
            when(mapper.toResponse(b)).thenReturn(rb);

            StepVerifier.create(service.list(null, null)).expectNext(ra, rb).verifyComplete();

            verify(repo).findAll(eq(SORT));
        }

        @Test
        @DisplayName("list: by level only -> findByLevel(sorted)")
        void list_byLevel() {
            AdminLevel level = AdminLevel.DISTRICT;
            AdminArea a = entity("01-01", level, "01", "A", "A");
            AdminAreaResponse ra = resp("01-01", "A", "A");

            when(repo.findByLevel(eq(level), eq(SORT))).thenReturn(Flux.just(a));
            when(mapper.toResponse(a)).thenReturn(ra);

            StepVerifier.create(service.list(level, null)).expectNext(ra).verifyComplete();

            verify(repo).findByLevel(eq(level), eq(SORT));
        }

        @Test
        @DisplayName("list: by parent only -> findByParentCode(sorted)")
        void list_byParent() {
            String parent = "01";
            AdminArea a = entity("01-01", AdminLevel.DISTRICT, parent, "A", "A");
            AdminAreaResponse ra = resp("01-01", "A", "A");

            when(repo.findByParentCode(eq(parent), eq(SORT))).thenReturn(Flux.just(a));
            when(mapper.toResponse(a)).thenReturn(ra);

            StepVerifier.create(service.list(null, parent)).expectNext(ra).verifyComplete();

            verify(repo).findByParentCode(eq(parent), eq(SORT));
        }

        @Test
        @DisplayName("list: by level and parent -> findByLevelAndParentCode(sorted)")
        void list_byLevelAndParent() {
            AdminLevel level = AdminLevel.COMMUNE;
            String parent = "01-01";
            AdminArea a = entity("01-01-01", level, parent, "A", "A");
            AdminAreaResponse ra = resp("01-01-01", "A", "A");

            when(repo.findByLevelAndParentCode(eq(level), eq(parent), eq(SORT))).thenReturn(Flux.just(a));
            when(mapper.toResponse(a)).thenReturn(ra);

            StepVerifier.create(service.list(level, parent)).expectNext(ra).verifyComplete();

            verify(repo).findByLevelAndParentCode(eq(level), eq(parent), eq(SORT));
        }
    }

    // ===========================================================
    // delete()
    // ===========================================================
    @Nested
    class Delete {

        @Test
        @DisplayName("delete: not found -> error; children check & delete not called")
        void delete_notFound() {
            String code = "01";
            when(repo.existsById(code)).thenReturn(Mono.just(false));

            StepVerifier.create(service.delete(code)).expectErrorSatisfies(err -> {
                Assertions.assertTrue(err instanceof AdminAreaNotFoundException);
                Assertions.assertTrue(err.getMessage().contains("not found"));
            }).verify();

            verify(repo).existsById(code);
            verify(repo, never()).existsByParentCode(anyString());
            verify(repo, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("delete: has children -> error; deleteById not called")
        void delete_childrenExist() {
            String code = "12";
            when(repo.existsById(code)).thenReturn(Mono.just(true));
            when(repo.existsByParentCode(code)).thenReturn(Mono.just(true));

            StepVerifier.create(service.delete(code)).expectErrorSatisfies(err -> {
                Assertions.assertTrue(err instanceof ChildrenExistException);
                Assertions.assertTrue(err.getMessage().contains("children exist"));
            }).verify();

            verify(repo).existsById(code);
            verify(repo).existsByParentCode(code);
            verify(repo, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("delete: exists and no children -> deleteById completes")
        void delete_success() {
            String code = "34";
            when(repo.existsById(code)).thenReturn(Mono.just(true));
            when(repo.existsByParentCode(code)).thenReturn(Mono.just(false));
            when(repo.deleteById(code)).thenReturn(Mono.empty());

            StepVerifier.create(service.delete(code)).verifyComplete();

            verify(repo).existsById(code);
            verify(repo).existsByParentCode(code);
            verify(repo).deleteById(code);
        }
    }

    // ===========================================================
    // update()
    // ===========================================================
    @Nested
    class Update {

        @Test
        @DisplayName("update: found -> mapper.update + save + response")
        void update_success() {
            String code = "01-02"; // use "0102" if dashless
            AdminArea existing = entity(code, AdminLevel.DISTRICT, "01", "oldKh", "oldEn");
            AdminArea saved = entity(code, AdminLevel.DISTRICT, "01", "newKh", "newEn");
            AdminAreaResponse out = resp(code, "newKh", "newEn");
            AdminAreaUpdateRequest req = new AdminAreaUpdateRequest("newKh", "newEn");

            when(repo.findById(code)).thenReturn(Mono.just(existing));

            doAnswer(inv -> {
                AdminArea target = inv.getArgument(0, AdminArea.class);
                AdminAreaUpdateRequest r = inv.getArgument(1, AdminAreaUpdateRequest.class);
                target.setNameKh(r.getNameKh());
                target.setNameEn(r.getNameEn());
                return null;
            }).when(mapper).update(any(AdminArea.class), any(AdminAreaUpdateRequest.class));

            when(repo.save(existing)).thenReturn(Mono.just(saved));
            when(mapper.toResponse(saved)).thenReturn(out);

            StepVerifier.create(service.update(code, req)).expectNext(out).verifyComplete();

            verify(repo).findById(code);
            verify(mapper).update(eq(existing), eq(req));
            verify(repo).save(existing);
            verify(mapper).toResponse(saved);
        }

        @Test
        @DisplayName("update: not found -> error")
        void update_notFound() {
            String code = "NF";
            when(repo.findById(code)).thenReturn(Mono.empty());

            StepVerifier.create(service.update(code, new AdminAreaUpdateRequest("a", "b")))
            .expectError(AdminAreaNotFoundException.class)
            .verify();

            verify(repo).findById(code);
            verify(repo, never()).save(any());
        }
    }
}
