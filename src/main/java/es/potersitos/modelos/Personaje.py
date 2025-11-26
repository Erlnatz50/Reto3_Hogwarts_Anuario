from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class Personaje:
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
    image_blob: bytes = field(default=None)  # para almacenar la imagen descargada
