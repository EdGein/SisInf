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

import jakarta.persistence.*;
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
}