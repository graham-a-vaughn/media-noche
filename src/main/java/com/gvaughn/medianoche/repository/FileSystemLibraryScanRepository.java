package com.gvaughn.medianoche.repository;

import com.gvaughn.medianoche.domain.FileSystemLibraryScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by graham on 6/2/17.
 * <p>
 * <p>
 * Media Project - streaming media system.
 * Copyright (C) 2017  Graham Vaughn
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
@SuppressWarnings("unused")
@Repository
public interface FileSystemLibraryScanRepository extends JpaRepository<FileSystemLibraryScan,Long> {
}
