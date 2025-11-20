package br.com.gabrielcaio.verso.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolesWithIdAndName
{
    private Long id;
    private String name;
}
