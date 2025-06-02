/*
MIT License

Copyright (c) 2025, Nuno Datia, ISEL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package isel.sisinf.jpa;

import isel.sisinf.model.Dock;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

public class Dal
{
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("dal-lab");
    //For Demonstration purpose only
    public static String version(){ return "1.0";}

    public static void insertRider(String name, String email, int taxnumber, String typeOfCard, double credit) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.createNativeQuery("""
                INSERT INTO RIDER (name, email, taxnumber, typeofcard, credit, dtregister)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """)
                    .setParameter(1, name)
                    .setParameter(2, email)
                    .setParameter(3, taxnumber)
                    .setParameter(4, typeOfCard)
                    .setParameter(5, credit)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    public static void listAllRiders() {
        EntityManager em = emf.createEntityManager();

        try {
            List<Object[]> rows = em.createNativeQuery("""
            SELECT name, email, taxnumber, dtregister, credit, typeofcard 
            FROM RIDER
            ORDER BY name
        """).getResultList();

            System.out.printf("%-20s %-30s %-12s %-20s %-8s %-10s\n",
                    "Nome", "Email", "NIF", "Data Registo", "Saldo", "Passe");

            for (Object[] row : rows) {
                System.out.printf("%-20s %-30s %-12s %-20s %-8s %-10s\n",
                        row[0], row[1], row[2], row[3], row[4], row[5]);
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar clientes: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    public static void listDocksWithOccupancy() {
        EntityManager em = emf.createEntityManager();

        try {
            List<Object[]> results = em.createNativeQuery("""
            SELECT s.id, s.latitude, s.longitude, fx_dock_occupancy(s.id::int) AS occupancy
            FROM STATION s
            ORDER BY s.id
        """).getResultList();

            System.out.printf("%-10s %-12s %-12s %-12s\n", "StationID", "Latitude", "Longitude", "Occupancy");
            for (Object[] row : results) {
                System.out.printf("%-10s %-12s %-12s %-12.2f\n",
                        row[0], row[1], row[2], row[3]);
            }

        } catch (Exception e) {
            System.err.println("Erro ao listar docas: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    public static void startTrip(int dockId, int clientId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.createNativeQuery("CALL startTrip(?, ?)")
                    .setParameter(1, dockId)
                    .setParameter(2, clientId)
                    .executeUpdate();
            tx.commit();
            System.out.println("Viagem iniciada com sucesso.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Erro ao iniciar a viagem: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    public static void parkScooter(int clientId, int dockId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Find ongoing trip
            Object[] trip = (Object[]) em.createNativeQuery("""
            SELECT dinitial, scooter FROM TRAVEL 
            WHERE client = ? AND dfinal IS NULL
        """).setParameter(1, clientId)
                    .getSingleResult();

            Timestamp dinitial = (Timestamp) trip[0];
            int scooterId = (int) trip[1];

            // 2. Get dock and attach optimistic lock
            Dock dock = em.find(Dock.class, dockId, LockModeType.OPTIMISTIC);
            em.refresh(dock);
            if (!"free".equals(dock.getState())) {
                throw new RuntimeException("Doca não está livre.");
            }

            // 3. Update dock
            dock.setScooter(scooterId);
            dock.setState("occupy");

            // 4. Update trip
            em.createNativeQuery("""
            UPDATE TRAVEL SET dfinal = CURRENT_TIMESTAMP, stfinal = ?
            WHERE client = ? AND dinitial = ?
        """)
                    .setParameter(1, dockId)
                    .setParameter(2, clientId)
                    .setParameter(3, dinitial)
                    .executeUpdate();

            tx.commit();
            System.out.println("Trotineta estacionada com sucesso.");
        } catch (OptimisticLockException e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Erro: Conflito de concorrência ao tentar estacionar.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Erro: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}