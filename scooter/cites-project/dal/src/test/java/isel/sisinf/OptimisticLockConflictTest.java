package isel.sisinf;

import isel.sisinf.model.Dock;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;

import junit.framework.TestCase;

public class OptimisticLockConflictTest extends TestCase {

    private static EntityManagerFactory emf;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (emf == null)
            emf = Persistence.createEntityManagerFactory("dal-lab");
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    public void testOptimisticLockConflict() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        EntityTransaction tx1 = em1.getTransaction();
        EntityTransaction tx2 = em2.getTransaction();

        try {
            // Begin transaction 1
            tx1.begin();
            Dock dock1 = em1.find(Dock.class, 2, LockModeType.OPTIMISTIC);
            dock1.setState("under maintenance");

            // Begin transaction 2 and commit it first
            tx2.begin();
            Dock dock2 = em2.find(Dock.class, 2, LockModeType.OPTIMISTIC);
            dock2.setState("occupy");
            tx2.commit();  // This updates the version

            // Commit tx1 — should throw OptimisticLockException
            try {
                tx1.commit();
                fail("Expected OptimisticLockException not thrown");
            } catch (OptimisticLockException e) {
                // Expected
                System.out.println("Caught expected optimistic lock conflict: " + e.getMessage());
            }

        } finally {
            if (tx1.isActive()) tx1.rollback();
            if (tx2.isActive()) tx2.rollback();
            em1.close();
            em2.close();
        }
    }
}
