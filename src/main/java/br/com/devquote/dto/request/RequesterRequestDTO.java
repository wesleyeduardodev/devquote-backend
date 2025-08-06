package br.com.devquote.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequesterRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 200, message = "Email must be at most 200 characters")
    private String email;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @Pattern(regexp = "^\\+?[0-9\\-(). ]*$", message = "Invalid phone number")
    private String phone;
}