package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RepositoryManagerTest {

    @Mock
    private ManagedRepository mockRepository;

    @Mock
    private ManagedRepository secondMockRepository;

    @Mock
    private ManagedRepositoryFactory mockFactory;

    @InjectMocks
    private RepositoryManagerImpl repositoryManager;

    private final String testName = "testRepository";

    @Test
    public void testWithRepository_withFunction_shouldCallFunctionOnRepository() {
        when(mockFactory.getRepositoryByName(testName)).thenReturn(mockRepository);
        Function<ManagedRepository, String> mockFunction = mock(repository -> "result");
        repositoryManager.withRepository(testName, mockFunction);

        var inOrder = inOrder(mockFunction, mockRepository);
        inOrder.verify(mockFunction).apply(mockRepository);
        inOrder.verify(mockRepository).close();
    }

    @Test
    public void testWithRepository_functionThrowsException_shouldStillCloseRepository() {
        when(mockFactory.getRepositoryByName(testName)).thenReturn(mockRepository);
        Function<ManagedRepository, String> mockFunction = mock(repository -> {
            throw new RepositoryAccessException("test");
        });
        Assertions.assertThatThrownBy(() -> repositoryManager.withRepository(testName, mockFunction))
                .isInstanceOf(RepositoryAccessException.class);
        var inOrder = inOrder(mockFunction, mockRepository);
        inOrder.verify(mockFunction).apply(mockRepository);
        inOrder.verify(mockRepository).close();
    }

    @Test
    public void testConsumeRepository_withConsumer_shouldCallConsumerOnRepository() {
        when(mockFactory.getRepositoryByName(testName)).thenReturn(mockRepository);
        @SuppressWarnings("unchecked")
        Consumer<ManagedRepository> mockConsumer = mock(Consumer.class);
        repositoryManager.consumeRepository(testName, mockConsumer);

        var inOrder = inOrder(mockConsumer, mockRepository);
        inOrder.verify(mockConsumer).accept(mockRepository);
        inOrder.verify(mockRepository).close();
    }

    @Test
    public void testConsumeRepository_consumerThrowsException_shouldStillCloseRepository() {
        when(mockFactory.getRepositoryByName(testName)).thenReturn(mockRepository);
        Consumer<ManagedRepository> mockConsumer = mock(repository -> {
            throw new RepositoryAccessException("test");
        });
        Assertions.assertThatThrownBy(() -> repositoryManager.consumeRepository(testName, mockConsumer))
                .isInstanceOf(RepositoryAccessException.class);
        var inOrder = inOrder(mockConsumer, mockRepository);
        inOrder.verify(mockConsumer).accept(mockRepository);
        inOrder.verify(mockRepository).close();
    }


    @Test
    public void testWithAllRepositories_withFunction_shouldCallFunctionOnRepositories() {
        var mockRepositories = List.of(mockRepository, secondMockRepository);
        when(mockFactory.getAllRepositories()).thenReturn(mockRepositories);
        Function<List<ManagedRepository>, String> mockFunction = mock(repositories -> "result");
        repositoryManager.withAllRepositories(mockFunction);
        var inOrder = inOrder(mockFunction, mockRepository, secondMockRepository);
        inOrder.verify(mockFunction).apply(mockRepositories);
        inOrder.verify(mockRepository).close();
        inOrder.verify(secondMockRepository).close();
    }

    @Test
    public void testWithAllRepositories_functionThrowsException_shouldStillCloseRepositories() {
        var mockRepositories = List.of(mockRepository, secondMockRepository);
        when(mockFactory.getAllRepositories()).thenReturn(mockRepositories);
        Function<List<ManagedRepository>, String> mockFunction = mock(repositories -> {
            throw new RepositoryAccessException("test");
        });
        Assertions.assertThatThrownBy(() -> repositoryManager.withAllRepositories(mockFunction))
                .isInstanceOf(RepositoryAccessException.class);
        var inOrder = inOrder(mockFunction, mockRepository, secondMockRepository);
        inOrder.verify(mockFunction).apply(mockRepositories);
        inOrder.verify(mockRepository).close();
        inOrder.verify(secondMockRepository).close();
    }

    @Test
    public void testWithAllRepositories_noRepositories_shouldBeCalledOnEmptyList() {
        when(mockFactory.getAllRepositories()).thenReturn(Collections.emptyList());
        Function<List<ManagedRepository>, String> mockFunction = mock(repositories -> "result");
        repositoryManager.withAllRepositories(mockFunction);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ManagedRepository>> functionArg = ArgumentCaptor.forClass(List.class);
        verify(mockFunction).apply(functionArg.capture());
        Assertions.assertThat(functionArg.getValue()).isEmpty();
    }

    @Test
    public void testConsumeAllRepositories_withConsumer_shouldCallConsumerOnRepositories() {
        var mockRepositories = List.of(mockRepository, secondMockRepository);
        when(mockFactory.getAllRepositories()).thenReturn(mockRepositories);
        @SuppressWarnings("unchecked")
        Consumer<List<ManagedRepository>> mockConsumer = mock(Consumer.class);
        repositoryManager.consumeAllRepositories(mockConsumer);

        var inOrder = inOrder(mockConsumer, mockRepository, secondMockRepository);
        inOrder.verify(mockConsumer).accept(mockRepositories);
        inOrder.verify(mockRepository).close();
        inOrder.verify(secondMockRepository).close();
    }

    @Test
    public void testConsumeAllRepositories_consumerThrowsException_shouldStillCloseRepositories() {
        var mockRepositories = List.of(mockRepository, secondMockRepository);
        when(mockFactory.getAllRepositories()).thenReturn(mockRepositories);
        Consumer<List<ManagedRepository>> mockConsumer = mock(repositories -> {
            throw new RepositoryAccessException("test");
        });
        Assertions.assertThatThrownBy(() -> repositoryManager.consumeAllRepositories(mockConsumer))
                .isInstanceOf(RepositoryAccessException.class);
        var inOrder = inOrder(mockConsumer, mockRepository, secondMockRepository);
        inOrder.verify(mockConsumer).accept(mockRepositories);
        inOrder.verify(mockRepository).close();
        inOrder.verify(secondMockRepository).close();
    }


    @Test
    public void testConsumeAllRepositories_noRepositories_shouldBeCalledOnEmptyList() {
        when(mockFactory.getAllRepositories()).thenReturn(Collections.emptyList());
        @SuppressWarnings("unchecked")
        Consumer<List<ManagedRepository>> mockFunction = mock(Consumer.class);
        repositoryManager.consumeAllRepositories(mockFunction);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ManagedRepository>> functionArg = ArgumentCaptor.forClass(List.class);
        verify(mockFunction).accept(functionArg.capture());
        Assertions.assertThat(functionArg.getValue()).isEmpty();
    }
}
