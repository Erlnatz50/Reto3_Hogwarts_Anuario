from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class Personaje:
    """
    Representa un personaje del universo de Harry Potter con sus atributos principales.

    Attributes:
        id (str): Identificador único del personaje.
        type (str): Tipo de entidad (por ejemplo, "personaje").
        slug (str): Nombre corto o "slug" para URL.
        alias_names (List[str]): Lista de nombres alternativos o apodos.
        animagus (Optional[str]): Forma de animago, si aplica.
        blood_status (Optional[str]): Estado de sangre (pura, mestiza, etc.).
        boggart (Optional[str]): Forma de boggart del personaje.
        born (Optional[str]): Fecha de nacimiento.
        died (Optional[str]): Fecha de fallecimiento.
        eye_color (Optional[str]): Color de ojos.
        family_members (List[str]): Lista de miembros de la familia.
        gender (Optional[str]): Género del personaje.
        hair_color (Optional[str]): Color de cabello.
        height (Optional[str]): Altura del personaje.
        house (Optional[str]): Casa de Hogwarts a la que pertenece.
        image (Optional[str]): URL de la imagen del personaje.
        jobs (List[str]): Lista de ocupaciones o trabajos del personaje.
        marital_status (Optional[str]): Estado civil.
        name (Optional[str]): Nombre completo.
        nationality (Optional[str]): Nacionalidad.
        patronus (Optional[str]): Patronus del personaje.
        romances (List[str]): Lista de relaciones románticas.
        skin_color (Optional[str]): Color de piel.
        species (Optional[str]): Especie del personaje.
        titles (List[str]): Lista de títulos o rangos.
        wands (List[str]): Lista de varitas asociadas.
        weight (Optional[str]): Peso del personaje.
        wiki (Optional[str]): URL de la página de Wikipedia o fuente.
        image_blob (Optional[bytes]): Imagen en formato binario, por defecto None.
    """
    id: str
    type: str
    slug: str
    alias_names: List[str]
    animagus: Optional[str]
    blood_status: Optional[str]
    boggart: Optional[str]
    born: Optional[str]
    died: Optional[str]
    eye_color: Optional[str]
    family_members: List[str]
    gender: Optional[str]
    hair_color: Optional[str]
    height: Optional[str]
    house: Optional[str]
    image: Optional[str]
    jobs: List[str]
    marital_status: Optional[str]
    name: Optional[str]
    nationality: Optional[str]
    patronus: Optional[str]
    romances: List[str]
    skin_color: Optional[str]
    species: Optional[str]
    titles: List[str]
    wands: List[str]
    weight: Optional[str]
    wiki: Optional[str]
    image_blob: Optional[bytes] = field(default=None)
