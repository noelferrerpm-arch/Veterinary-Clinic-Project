package cat.tecnocampus.security.authentication;

import cat.tecnocampus.domain.Person;
import cat.tecnocampus.domain.Veterinarian;
import cat.tecnocampus.domain.exceptions.ProfileDoesNotExistException;
import cat.tecnocampus.persistence.PersonRepository;
import cat.tecnocampus.persistence.VeterinarianRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class ProfileDetailsService implements UserDetailsService {
    private final VeterinarianRepository veterinarianRepository;
    private PersonRepository personRepository;

    public ProfileDetailsService(PersonRepository profileRepository, VeterinarianRepository veterinarianRepository) {
        this.personRepository = profileRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws ProfileDoesNotExistException {
        Person profile = personRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ProfileDoesNotExistException("Profile not Found with nickname: " + userId));

        return new ProfileDetails(profile);
    }

}