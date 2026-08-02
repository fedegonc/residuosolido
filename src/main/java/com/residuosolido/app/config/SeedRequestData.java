package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;

import java.util.List;

public class SeedRequestData {

    public static void seedAll(RequestRepository repo, User u1, User u2, User u3, User u4,
                               User o1, User o2, User o3, User o4, User o5, User o6) {
        SeedDataFactory.createRequest(repo, u1, o1, "Calle 18 de Julio 123", "Frente al supermercado", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        SeedDataFactory.createRequest(repo, u2, o4, "Rua dos Andradas 456", "Esquina con farmacia", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO), RequestStatus.IN_PROGRESS, TimeSlot.MANANA);
        SeedDataFactory.createRequest(repo, u1, o1, "Av. Sarandí 789", "Portón verde", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.TARDE);
        SeedDataFactory.createRequest(repo, u2, null, "Rua Flores da Cunha 32", null, City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL), RequestStatus.PENDING, null);
        SeedDataFactory.createRequest(repo, null, null, "Calle Misiones 55", "Casa con rejas", City.RIVERA,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO, MaterialCategory.METAL), RequestStatus.REJECTED, null,
                "Carlos Guest", "+598 99 888 777");
        SeedDataFactory.createRequest(repo, u3, o2, "Calle Independencia 202", "Al lado de la plaza", City.RIVERA,
                List.of(MaterialCategory.METAL), RequestStatus.PENDING, null);
        SeedDataFactory.createRequest(repo, u4, o5, "Rua Marechal Deodoro 77", "Portão azul", City.LIVRAMENTO,
                List.of(MaterialCategory.VIDRIO, MaterialCategory.PLASTICO), RequestStatus.IN_PROGRESS, TimeSlot.TARDE);
        SeedDataFactory.createRequest(repo, u3, o3, "Calle Rivera 450", "Frente a la escuela", City.RIVERA,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.COMPLETED, TimeSlot.MANANA);
        SeedDataFactory.createRequest(repo, u4, o6, "Rua Bento Gonçalves 890", "Galpão vermelho", City.LIVRAMENTO,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.METAL), RequestStatus.PENDING, null);
        SeedDataFactory.createRequest(repo, null, o1, "Calle Ituzaingó 30", "Departamento 2B", City.RIVERA,
                List.of(MaterialCategory.PLASTICO), RequestStatus.PENDING, null, "Ana Guest", "+598 99 444 555");
        SeedDataFactory.createRequest(repo, u1, o2, "Calle Amethyst 15", "Casa esquinera", City.RIVERA,
                List.of(MaterialCategory.METAL, MaterialCategory.PLASTICO), RequestStatus.COMPLETED, TimeSlot.NOCHE);
        SeedDataFactory.createRequest(repo, u2, o6, "Rua dos Imigrantes 120", "Frente ao mercado", City.LIVRAMENTO,
                List.of(MaterialCategory.PAPEL, MaterialCategory.VIDRIO), RequestStatus.REJECTED, null);
    }
}
