package com.example.composers.services;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.composers.exceptions.ComposerNotFoundException;
import com.example.composers.exceptions.ComposerNotRegisteredException;
import com.example.composers.exceptions.DuplicateComposerException;
import com.example.composers.exceptions.PieceNotFoundException;
import com.example.composers.models.Composer;
import com.example.composers.models.Instrumentation;
import com.example.composers.models.MusicalPiece;

// Servicio con la logica de los compositores y las obras (antes era la clase Manager)
// Ya no guarda listas en memoria: cada vez que necesita los datos los pide al CrudOperator
@Service
public class ComposerService {

    // La carpeta de los .csv se lee de application.properties (app.data-dir), asi cambia segun donde se ejecute
    @Value("${app.data-dir}/composers.csv")
    private String COMPOSERS_CSV;
    @Value("${app.data-dir}/pieces.csv")
    private String PIECES_CSV;

    // Se inyecta la interfaz, no la clase. Con @Qualifier("csv") se elige la implementacion de los .csv
    @Autowired
    @Qualifier("csv")
    private CrudOperator crudOperator;

    // Obra que espera a que se registre su compositor (extension: compositor no registrado)
    private MusicalPiece pendingPiece = null;

    // Lee los compositores del .csv y les añade sus obras
    public List<Composer> getComposers() {
        List<Composer> composers = Arrays.stream(crudOperator.read(COMPOSERS_CSV))
                .map(line -> new Composer(line.split(";", -1)))
                .toList();
        for (MusicalPiece piece : readPieces()) {
            composers.stream()
                    .filter(c -> c.getName().equals(piece.getComposer()))
                    .findFirst()
                    .ifPresent(c -> c.addPiece(piece));
        }
        return composers;
    }

    // Lee todas las obras del .csv
    private List<MusicalPiece> readPieces() {
        return Arrays.stream(crudOperator.read(PIECES_CSV))
                .map(line -> new MusicalPiece(line.split(";", -1)))
                .toList();
    }

    // Pasa un texto a minusculas y sin tildes para poder comparar nombres escritos por el usuario
    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    public MusicalPiece getPendingPiece() {
        return pendingPiece;
    }

    public void setPendingPiece(MusicalPiece pendingPiece) {
        this.pendingPiece = pendingPiece;
    }

    // Devuelve los compositores ordenados por nombre artistico (sin distinguir mayusculas ni tildes)
    public List<Composer> getComposersSortedByName() {
        List<Composer> sorted = new ArrayList<>(getComposers());
        Collections.sort(sorted, Comparator.comparing(c -> normalize(c.getName())));
        return sorted;
    }

    // Obtiene todas las obras de todos los compositores en una sola lista, ordenadas por titulo
    public List<MusicalPiece> getAllPieces() {
        return getComposers().stream()
                .flatMap(c -> c.getPieces().stream())
                .sorted(Comparator.comparing(p -> normalize(p.getTitle())))
                .toList();
    }

    // Lista las nacionalidades que hay entre los compositores, sin repetir y ordenadas
    public List<String> getNationalities() {
        return getComposers().stream().map(Composer::getNationality).distinct().sorted().toList();
    }

    // Agrupa los compositores por nacionalidad y devuelve los grupos ordenados por nacionalidad
    public List<Map.Entry<String, List<Composer>>> groupByNationality() {
        return getComposers().stream()
                .collect(Collectors.groupingBy(Composer::getNationality))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    // Busca un compositor por su nombre artistico, sin distinguir mayusculas ni tildes
    public Optional<Composer> findComposerByName(String name) {
        String wanted = normalize(name);
        return getComposers().stream().filter(c -> normalize(c.getName()).equals(wanted)).findFirst();
    }

    // Devuelve el compositor o lanza ComposerNotFoundException si no existe
    public Composer getComposer(String name) {
        return findComposerByName(name).orElseThrow(() -> new ComposerNotFoundException(name));
    }

    // Devuelve la obra o lanza PieceNotFoundException si no existe
    public MusicalPiece getPiece(int id) {
        return findPieceById(id).orElseThrow(() -> new PieceNotFoundException(id));
    }

    // Busca una obra por su identificador
    public Optional<MusicalPiece> findPieceById(int id) {
        return readPieces().stream().filter(p -> p.getId() == id).findFirst();
    }

    // Create: registra un compositor nuevo. Si ya existe uno con ese nombre lanza una excepcion
    public void addComposer(Composer composer) {
        composer.completeNames();
        if (findComposerByName(composer.getName()).isPresent()) {
            throw new DuplicateComposerException(composer.getName());
        }
        crudOperator.create(composer.toCsv(), COMPOSERS_CSV);
    }

    // Update: sustituye un compositor por su version editada
    public void editComposer(String originalName, Composer edited) {
        Optional<Composer> original = findComposerByName(originalName);
        if (original.isEmpty()) {
            throw new ComposerNotFoundException(originalName);
        }
        edited.completeNames();
        Optional<Composer> sameName = findComposerByName(edited.getName());
        if (sameName.isPresent() && !sameName.get().getName().equals(original.get().getName())) {
            throw new DuplicateComposerException(edited.getName());
        }
        edited.setPhotos(original.get().getPhotos());
        crudOperator.update(original.get().getName(), edited.toCsv(), COMPOSERS_CSV);
        // Si ha cambiado el nombre artistico, sus obras tienen que apuntar al nombre nuevo
        for (MusicalPiece piece : original.get().getPieces()) {
            piece.setComposer(edited.getName());
            crudOperator.update(String.valueOf(piece.getId()), piece.toCsv(), PIECES_CSV);
        }
    }

    // Añade una foto al compositor (la ruta con la que se ve en la web, por ejemplo /files/123.jpg)
    public void addPhoto(String name, String photoPath) {
        Composer composer = getComposer(name);
        composer.getPhotos().add(photoPath);
        crudOperator.update(composer.getName(), composer.toCsv(), COMPOSERS_CSV);
    }

    // Delete: elimina un compositor junto con todas sus obras
    public void deleteComposer(String name) {
        Optional<Composer> composer = findComposerByName(name);
        if (composer.isEmpty()) {
            return;
        }
        composer.get().getPieces().forEach(p -> crudOperator.delete(String.valueOf(p.getId()), PIECES_CSV));
        crudOperator.delete(composer.get().getName(), COMPOSERS_CSV);
    }

    // Create: registra una obra nueva con el siguiente id libre
    public void addPiece(MusicalPiece piece) {
        int nextId = readPieces().stream().mapToInt(MusicalPiece::getId).max().orElse(0) + 1;
        piece.setId(nextId);
        Optional<Composer> composer = findComposerByName(piece.getComposer());
        if (composer.isEmpty()) {
            throw new ComposerNotRegisteredException(piece.getComposer());
        }
        piece.setComposer(composer.get().getName());
        crudOperator.create(piece.toCsv(), PIECES_CSV);
    }

    // Update: sustituye una obra por su version editada (identificada por su id)
    public void editPiece(MusicalPiece edited) {
        if (findPieceById(edited.getId()).isEmpty()) {
            throw new PieceNotFoundException(edited.getId());
        }
        Optional<Composer> composer = findComposerByName(edited.getComposer());
        if (composer.isEmpty()) {
            throw new ComposerNotRegisteredException(edited.getComposer());
        }
        edited.setComposer(composer.get().getName());
        crudOperator.update(String.valueOf(edited.getId()), edited.toCsv(), PIECES_CSV);
    }

    // Delete: elimina una obra
    public void deletePiece(int id) {
        crudOperator.delete(String.valueOf(id), PIECES_CSV);
    }

    // Quita las tildes de un texto (sin cambiar mayusculas), para que "Frederic" encuentre "Frédéric"
    private static String stripAccents(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    // Crea el patron de busqueda a partir de lo que ha escrito el usuario
    private static Pattern namePattern(String text) {
        String regex = stripAccents(text.trim());
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(Pattern.quote(regex), Pattern.CASE_INSENSITIVE);
        }
    }

    // Busca obras por titulo con texto o expresion regular (sin distinguir mayusculas ni tildes)
    public List<MusicalPiece> searchPieces(String text) {
        Pattern pattern = namePattern(text);
        return readPieces().stream()
                .filter(p -> pattern.matcher(stripAccents(p.getTitle())).find()
                        || p.getAlternativeTitles().stream().anyMatch(t -> pattern.matcher(stripAccents(t)).find()))
                .sorted(Comparator.comparing(p -> normalize(p.getTitle())))
                .toList();
    }

    // Busca compositores combinando condiciones opcionales, ordenados por fecha de nacimiento
    public List<Composer> search(String name, String nationality, boolean aliveOnly, boolean manyPieces) {
        boolean filterName = name != null && !name.isBlank();
        Pattern namePattern = filterName ? namePattern(name) : null;
        boolean filterNationality = nationality != null && !nationality.isBlank();
        String wanted = filterNationality ? normalize(nationality) : "";
        return getComposers().stream()
                .filter(c -> !filterName || namePattern.matcher(stripAccents(c.getName())).find()
                        || namePattern.matcher(stripAccents(c.getRealName())).find())
                .filter(c -> !filterNationality || normalize(c.getNationality()).equals(wanted))
                .filter(c -> !aliveOnly || c.isAlive())
                .filter(c -> !manyPieces || c.getPieces().size() > 3)
                .sorted(Comparator.comparing(Composer::getBirthDate))
                .toList();
    }

    // Agrupa las obras de un compositor por instrumentacion
    public Map<Instrumentation, List<MusicalPiece>> groupPiecesByInstrumentation(Composer composer) {
        return composer.getPieces().stream().collect(Collectors.groupingBy(MusicalPiece::getInstrumentation));
    }
}
