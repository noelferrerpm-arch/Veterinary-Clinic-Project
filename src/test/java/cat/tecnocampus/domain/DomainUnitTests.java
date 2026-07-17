package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DomainUnitTests {

    @Test
    void settersAndGettersWork() {
        Person p = new Person();
        p.setPersonId(10L);
        p.setFirstName("Anna");
        p.setLastName("Gomez");
        p.setPhoneNumber("12345");
        p.setEmail("a@b.com");
        p.setAddress("Carrer 1");
        p.setPassword("secret");

        assertEquals(Long.valueOf(10L), p.getPersonId());
        assertEquals("Anna", p.getFirstName());
        assertEquals("Gomez", p.getLastName());
        assertEquals("12345", p.getPhoneNumber());
        assertEquals("a@b.com", p.getEmail());
        assertEquals("Carrer 1", p.getAddress());
        assertEquals("secret", p.getPassword());
    }

    @Test
    void rolesSetterAndGetter() {
        Person p = new Person();
        Set<Role> roles = new HashSet<>();
        p.setRoles(roles);

        assertSame(roles, p.getRoles());
        assertTrue(p.getRoles().isEmpty());
    }

    @Test
    void licenseNumberValidationThrows() {
        Veterinarian v = new Veterinarian();
        assertThrows(InvalidDataException.class, () -> v.setLicenseNumber(0));
        assertThrows(InvalidDataException.class, () -> v.setLicenseNumber(-5));
    }

    @Test
    void yearsOfExperienceValidationThrows() {
        Veterinarian v = new Veterinarian();
        assertThrows(InvalidDataException.class, () -> v.setYearsOfExperience(-1));
    }

    @Test
    void settersAndGettersAndCollectionsWork() {
        Veterinarian v = new Veterinarian();

        // herencia de Person
        v.setFirstName("Pere");
        v.setLastName("Garcia");
        assertEquals("Pere", v.getFirstName());
        assertEquals("Garcia", v.getLastName());

        // campos propios
        v.setLicenseNumber(1234);
        v.setYearsOfExperience(10);
        assertEquals(1234, v.getLicenseNumber());
        assertEquals(10, v.getYearsOfExperience());

        // availability list via setter/getter
        List<Availability> availList = new ArrayList<>();
        v.setAvailability(availList);
        assertSame(availList, v.getAvailability());
        assertTrue(v.getAvailability().isEmpty());

        // intentar añadir una Availability (usa el constructor por defecto de la entidad)
        Availability a = new Availability();
        v.addAvailability(a);
        assertTrue(v.getAvailability().contains(a));

        // specialities list via setter/getter
        List<Speciality> specList = new ArrayList<>();
        v.setSpecialities(specList);
        assertSame(specList, v.getSpecialities());

        Speciality s = new Speciality();
        v.addSpeciality(s);
        assertTrue(v.getSpecialities().contains(s));

        // remove speciality por referencia
        v.removeSpeciality(s);
        assertFalse(v.getSpecialities().contains(s));
    }

    @Test
    void petPetOwnerPetTypeDomainTest() {
        // PetType
        PetType pt = new PetType();
        pt.setTypeId(1L);
        pt.setName("Dog");
        pt.setDescription("Canine");
        assertEquals(Long.valueOf(1L), pt.getTypeId());
        assertEquals("Dog", pt.getName());
        assertEquals("Canine", pt.getDescription());

        // PetOwner
        PetOwner owner = new PetOwner();
        owner.setPersonId(5L);
        owner.setFirstName("Laura");
        owner.setLoyaltyPoints(20);
        assertEquals(Long.valueOf(5L), owner.getPersonId());
        assertEquals("Laura", owner.getFirstName());
        assertEquals(20, owner.getLoyaltyPoints());

        // Pet: setters/getters, asociación con owner y colección de owner
        Pet p = new Pet();
        p.setPetId(2L);
        p.setName("Firulais");
        p.setDateOfBirth(java.time.LocalDate.of(2020, 1, 1));
        p.setGender("M");
        p.setBreed("Labrador");
        p.setColor("Brown");
        p.setWeight("12kg");
        p.setMicrochipId("MC123");
        p.setPetType(pt);

        assertEquals(Long.valueOf(2L), p.getPetId());
        assertEquals("Firulais", p.getName());
        assertEquals("Labrador", p.getBreed());
        assertEquals("Brown", p.getColor());
        assertEquals("12kg", p.getWeight());
        assertEquals("MC123", p.getMicrochipId());
        assertSame(pt, p.getPetType());

        // asociación bidireccional owner <-> pet usando add/remove de Owner
        owner.addPet(p);
        assertTrue(owner.getPets().contains(p));
        assertSame(owner, p.getPetOwner());

        owner.removePet(p);
        assertFalse(owner.getPets().contains(p));
        assertNull(p.getPetOwner());

        // Validaciones de breed (lanza InvalidDataException si nulo o blank)
        Pet p2 = new Pet();
        assertThrows(InvalidDataException.class, () -> p2.setBreed(""));
        assertThrows(InvalidDataException.class, () -> p2.setBreed(null));

        // Prescripciones: add/remove y validaciones
        MedicationPrescription mp = new MedicationPrescription();
        p.addPrescription(mp);
        assertTrue(p.getPrescriptions().contains(mp));

        // añadir null -> error
        assertThrows(InvalidDataException.class, () -> p.addPrescription(null));
        // añadir duplicado -> error
        assertThrows(InvalidDataException.class, () -> p.addPrescription(mp));

        // remover existente y comprobar estado
        p.removePrescription(mp);
        assertFalse(p.getPrescriptions().contains(mp));

        // remover null -> error
        assertThrows(InvalidDataException.class, () -> p.removePrescription(null));
        // remover no existente -> error
        assertThrows(InvalidDataException.class, () -> p.removePrescription(mp));
    }

    @Test
    void medicationAndMedicationBatchDomainTest() throws Exception {
        // Medication: constructor y getters
        Medication m = new Medication("Ibuprof", "Ibuprofen", 2, 1.5);
        assertEquals("Ibuprof", m.getName());
        assertEquals("Ibuprofen", m.getActiveIngredient());
        assertEquals(2, m.getDosageUnit());
        assertEquals(1.5, m.getUnitPrice(), 1e-6);

        // Validaciones de setters
        Medication m2 = new Medication();
        assertThrows(InvalidDataException.class, () -> m2.setName(null));
        assertThrows(InvalidDataException.class, () -> m2.setName(""));
        assertThrows(InvalidDataException.class, () -> m2.setActiveIngredient(""));
        assertThrows(InvalidDataException.class, () -> m2.setDosageUnit(0));
        assertThrows(InvalidDataException.class, () -> m2.setUnitPrice(0.0));
        assertThrows(InvalidDataException.class, () -> m2.setMedicationId(0L));
        assertThrows(InvalidDataException.class, () -> m2.setMedicationId(null));

        // Incompatibilidades: inicializar la lista por reflection para poder probar add/remove
        java.lang.reflect.Field incompatField = Medication.class.getDeclaredField("incompatibilities");
        incompatField.setAccessible(true);
        incompatField.set(m, new java.util.ArrayList<Medication>());

        Medication other = new Medication("Other", "AI", 1, 2.0);
        m.addIncompatibilities(other);
        assertTrue(m.getIncompatibilities().contains(other));
        // añadir duplicado -> error
        assertThrows(InvalidDataException.class, () -> m.addIncompatibilities(other));
        // añadir a sí mismo -> error
        assertThrows(InvalidDataException.class, () -> m.addIncompatibilities(m));
        // remover existente
        m.removeIncompatibilities(other);
        assertFalse(m.getIncompatibilities().contains(other));
        // remover no existente -> error
        assertThrows(InvalidDataException.class, () -> m.removeIncompatibilities(other));

        // checkIncompatibilities: preparar medicación A incompatible con B y una prescripción en la mascota
        Medication medA = new Medication("A", "AI", 1, 1.0);
        Medication medB = new Medication("B", "AI", 1, 1.0);
        incompatField.set(medA, new java.util.ArrayList<Medication>());
        medA.addIncompatibilities(medB);

        Pet pet = new Pet();
        MedicationPrescription mp = new MedicationPrescription();
        mp.setMedication(medB);
        pet.addPrescription(mp);
        assertFalse(medA.checkIncompatibilities(pet));
        // mascota sin prescripciones -> compatible
        Pet pet2 = new Pet();
        assertTrue(medA.checkIncompatibilities(pet2));

        // MedicationBatch: constructor y getters
        java.time.LocalDate received = java.time.LocalDate.now().minusDays(1);
        java.time.LocalDate expiry = java.time.LocalDate.now().plusDays(10);
        MedicationBatch batch = new MedicationBatch(1L, 100, received, expiry, 50, 0.5, "A1", 10);

        assertEquals(Long.valueOf(1L), batch.getMedicationId());
        assertEquals(100, batch.getLotNumber());
        assertEquals(received, batch.getReceivedDate());
        assertEquals(expiry, batch.getExpiryDate());
        assertEquals(50, batch.getInitialQuantity());
        assertEquals(50, batch.getCurrentQuantity());
        assertEquals(0.5, batch.getPurchagePricePerUnit(), 1e-6);
        assertEquals("A1", batch.getStorageLocation());
        assertEquals(10, batch.getReorderThreshold());

        // Validaciones de setters de MedicationBatch
        assertThrows(InvalidDataException.class, () -> batch.setMedicationId(null));
        assertThrows(InvalidDataException.class, () -> batch.setLotNumber(0));
        assertThrows(InvalidDataException.class, () -> batch.setInitialQuantity(0));
        assertThrows(InvalidDataException.class, () -> batch.setCurrentQuantity(0));
        assertThrows(InvalidDataException.class, () -> batch.setPurchagePricePerUnit(0.0));
        assertThrows(InvalidDataException.class, () -> batch.setStorageLocation(null));
        assertThrows(InvalidDataException.class, () -> batch.setReorderThreshold(0));

        // betterOption: comparar por fecha de expiración y cantidad
        java.time.LocalDate expiryLater = java.time.LocalDate.now().plusDays(20);
        MedicationBatch batch2 = new MedicationBatch(1L, 101, received, expiryLater, 30, 0.5, "A2", 5);
        // batch tiene expiry anterior y currentQuantity (50) > quantity(10) -> devuelve this
        MedicationBatch chosen = batch.betterOption(batch2, 10);
        assertSame(batch, chosen);
        // si la cantidad solicitada es mayor que currentQuantity -> devuelve toGive
        MedicationBatch chosen2 = batch.betterOption(batch2, 100);
        assertSame(batch2, chosen2);
    }

    @Test
    void invoiceAndInvoiceItemDomainTest() {
        // Invoice: setters/getters básicos
        Invoice inv = new Invoice();
        inv.setInvoiceId(1L);
        inv.setInvoiceDate(java.time.LocalDate.of(2025, 1, 1));
        inv.setTotalAmount(200.0);
        inv.setDiscountAmount(20.0);
        inv.setFinalAmount(180.0);
        inv.setStatus("PAID");

        PetOwner owner = new PetOwner();
        owner.setPersonId(3L);
        owner.setFirstName("Carlos");
        inv.setPetOwner(owner);

        assertEquals(Long.valueOf(1L), inv.getInvoiceId());
        assertEquals(java.time.LocalDate.of(2025, 1, 1), inv.getInvoiceDate());
        assertEquals(200.0, inv.getTotalAmount(), 1e-6);
        assertEquals(20.0, inv.getDiscountAmount(), 1e-6);
        assertEquals(180.0, inv.getFinalAmount(), 1e-6);
        assertEquals("PAID", inv.getStatus());
        assertSame(owner, inv.getPetOwner());

        // InvoiceItem: setters/getters y asociación manual con Invoice
        InvoiceItem item = new InvoiceItem();
        item.setItemId(10L);
        item.setDescription("Medication A");
        item.setQuantity(2);
        item.setUnitPrice(15.0);
        item.setItemTotal(30.0);

        // establecer asociación bidireccional manualmente (no hay helper en la entidad)
        inv.getItems().add(item);
        item.setInvoice(inv);

        assertSame(inv, item.getInvoice());
        assertTrue(inv.getItems().contains(item));

        // reemplazar la lista de items via setter/getter
        java.util.List<InvoiceItem> newItems = new java.util.ArrayList<>();
        inv.setItems(newItems);
        assertSame(newItems, inv.getItems());
        // el item anterior ya no está en la lista
        assertFalse(inv.getItems().contains(item));

        // payments: setter/getter de colección
        Payment pay = new Payment();
        java.util.List<Payment> payments = new java.util.ArrayList<>();
        payments.add(pay);
        inv.setPayments(payments);
        assertSame(payments, inv.getPayments());
        assertTrue(inv.getPayments().contains(pay));

        // discounts: setter/getter de colección
        Discount disc = new Discount();
        java.util.Set<Discount> discounts = new java.util.HashSet<>();
        discounts.add(disc);
        inv.setDiscounts(discounts);
        assertSame(discounts, inv.getDiscounts());
        assertTrue(inv.getDiscounts().contains(disc));
    }

}